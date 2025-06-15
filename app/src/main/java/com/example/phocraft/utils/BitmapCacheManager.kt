package com.example.phocraft.utils

import android.graphics.Bitmap
import androidx.collection.LruCache

object BitmapCacheManager {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 6
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache[key]
    }

    fun removeBitmapFromMemoryCache(key: String) {
        memoryCache.remove(key)
    }
}