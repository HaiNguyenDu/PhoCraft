package com.example.phocraft.utils

import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.core.graphics.scale
import kotlin.math.sqrt

object BitmapCacheManager {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 6
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        val newBitmap = checkSizeBitMap(bitmap)

        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, newBitmap)
        }
    }

    fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache[key]
    }

    fun removeBitmapFromMemoryCache(key: String) {
        memoryCache.remove(key)
    }

    fun checkSizeBitMap(bitmap: Bitmap): Bitmap {
        val byteCountKB = bitmap.byteCount / 1024
        val maxSizeKB = cacheSize / 2 - 50
        if (byteCountKB > maxSizeKB) {
            val scaleRatio = sqrt(maxSizeKB.toFloat() / byteCountKB.toFloat())
            val newWidth = (bitmap.width * scaleRatio).toInt()
            val newHeight = (bitmap.height * scaleRatio).toInt()
            return bitmap.scale(newWidth, newHeight, false)
        } else return bitmap
    }
}