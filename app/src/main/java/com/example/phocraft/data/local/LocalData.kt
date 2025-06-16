package com.example.phocraft.data.local

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.net.Uri
import android.provider.MediaStore
import com.example.phocraft.enum.ImageCategory
import com.example.phocraft.model.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale

class LocalData(private val context: Context) {

    fun saveImage(bitmap: Bitmap): Uri? {

        val vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("vi", "VN"))
        dateFormat.timeZone = vietnamTimeZone

        val currentTimeInVietnam = dateFormat.format(Date())

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "image_${currentTimeInVietnam}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            try {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            } catch (e: Exception) {
            }
        }
        return uri

    }

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
