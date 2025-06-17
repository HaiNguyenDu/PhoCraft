package com.example.phocraft.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException

class StickerRepository {
    fun loadStickersFromAssets(context: Context): List<Bitmap> {
        val stickers = mutableListOf<Bitmap>()
        val assetManager = context.assets

        try {
            val files = assetManager.list("stickers") ?: return emptyList()

            for (filename in files) {
                val inputStream = assetManager.open("stickers/$filename")
                val bitmap = BitmapFactory.decodeStream(inputStream)
                stickers.add(bitmap)
                inputStream.close()
            }
        } catch (e: IOException) {
        }

        return stickers
    }
}