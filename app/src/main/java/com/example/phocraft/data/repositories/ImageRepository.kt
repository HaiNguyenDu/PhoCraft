package com.example.phocraft.data.repositories

import android.content.Context
import com.example.phocraft.data.local.LocalData
import com.example.phocraft.enum.ImageCategory
import com.example.phocraft.model.Image

class ImageRepository(private val context: Context) {
    private val localData = LocalData(context)

    suspend fun getImagesFromMediaStore(context: Context, category: ImageCategory): List<Image> {
        return localData.getImagesFromMediaStore(context, category)
    }

}