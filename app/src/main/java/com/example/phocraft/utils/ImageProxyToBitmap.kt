package com.example.phocraft.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Window
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import com.example.phocraft.enum.CameraSize

fun imageProxyToBitmapSinglePlane(
    window: Window,
    imageProxy: ImageProxy,
    cameraSize: CameraSize,
): Bitmap? {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    imageProxy.close()

    var originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    val targetWidth = originalBitmap.width.toInt()
    val targeta = originalBitmap.height.toInt()
    val size = GetSizeCameraHelper.getSize(window, cameraSize)

    val targetHeight = size[0].toInt()

    if (targetHeight > originalBitmap.height) {

        val scale = size[0].toFloat() / originalBitmap.height
        val scaledWidth = (originalBitmap.width * scale).toInt()

        val newBitmap = originalBitmap.scale(scaledWidth, targetHeight)

        val xOffset = (scaledWidth - targetWidth) / 2
        return Bitmap.createBitmap(newBitmap, xOffset, 0, targetWidth, targetHeight)

    } else {
        val top = size[1].toInt()
        return Bitmap.createBitmap(originalBitmap, 0, top, targetWidth, targetHeight)
    }

}
