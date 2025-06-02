package com.example.phocraft.data.local

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.phocraft.enum.ImageCategory
import com.example.phocraft.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalData(private val context: Context) {
    suspend fun getImagesFromMediaStore(
        context: Context,
        category: ImageCategory,
    ): List<Image> {
        return withContext(Dispatchers.IO) {
            val imagesList = mutableListOf<Image>()
            val uriStore = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.IS_FAVORITE
            )

            var selection: String? = null
            var selectionArgs: Array<String>? = null
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            if (category == ImageCategory.FAVORITE) {
                selection = "${MediaStore.Images.Media.IS_FAVORITE} = ?"
                selectionArgs = arrayOf("1")
            }

            try {
                context.contentResolver.query(
                    uriStore,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dateAddedColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    val bucketDisplayNameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val isFavoriteColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.IS_FAVORITE)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val dateAdded = cursor.getLong(dateAddedColumn)
                        val bucketName = cursor.getString(bucketDisplayNameColumn)
                        val isFavorite = cursor.getInt(isFavoriteColumn) == 1
                        val uri = ContentUris.withAppendedId(uriStore, id)

                        val isSelfie = bucketName?.contains("selfie", ignoreCase = true) == true ||
                                bucketName?.contains("front", ignoreCase = true) == true ||
                                bucketName?.contains("camera", ignoreCase = true) == true

                        if (category == ImageCategory.SELFIES && !isSelfie) continue

                        imagesList.add(
                            Image(
                                uri = uri,
                                dateAdded = dateAdded,
                                isFavorite = isFavorite,
                                isSelfie = isSelfie
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            imagesList
        }
    }

}
