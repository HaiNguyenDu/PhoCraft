package com.example.phocraft.data.repositories

import android.content.Context
import android.graphics.Bitmap
import com.example.phocraft.data.local.LocalData
import com.example.phocraft.enum.ImageCategory
import com.example.phocraft.model.Image
import com.google.mlkit.vision.face.Face

class ImageRepository(private val context: Context) {
    private val localData = LocalData(context)

    suspend fun getImagesFromMediaStore(category: ImageCategory): List<Image> {
        return localData.getImagesFromMediaStore(context, category)
    }

    suspend fun saveImage(bitmap: Bitmap): Boolean {

        return localData.saveImage(bitmap)
    }

}