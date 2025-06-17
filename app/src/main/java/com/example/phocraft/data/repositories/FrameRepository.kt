package com.example.phocraft.data.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.IOException

class FrameRepository {
    fun getBitmapFromAssetConcise(context: Context, fileName: String): Bitmap? {
        return try {
            context.assets.open(fileName).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            Log.e("ImageAssetLoader", "Lỗi khi tải ảnh từ assets: $fileName", e)
            null
        }
    }

    fun getBitmapListFromAssetFolder(context: Context): List<Bitmap> {
        val bitmapList = mutableListOf<Bitmap>()
        try {

            val fileNames = context.assets.list("frame")
            if (fileNames.isNullOrEmpty()) {
                Log.w("ImageAssetLoader", "Thư mục assets frames trống hoặc không tồn tại.")
                return emptyList()
            }

            for (fileName in fileNames) {
                // Tạo đường dẫn đầy đủ và tải bitmap
                val fullPath = "frame/$fileName"
                val bitmap = getBitmapFromAssetConcise(context, fullPath)
                bitmap?.let {
                    bitmapList.add(it)
                }
            }
        } catch (e: IOException) {
            Log.e("ImageAssetLoader", "Lỗi khi truy cập thư mục assets: frames", e)
        }
        return bitmapList
    }
}