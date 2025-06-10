package com.example.phocraft.utils

import android.graphics.Bitmap
import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.graphics.scale
import com.example.phocraft.enum.CameraSize
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.R)
fun imageProxyToBitmapSinglePlane(
    window: Window,
    originalBitmap: Bitmap,
    cameraSize: CameraSize,
): Bitmap? {

    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height

    val metrics = window.windowManager.currentWindowMetrics
    val screenWidth = metrics.bounds.width()
    val screenHeight = metrics.bounds.height()


    val finalWidth = screenWidth
    val size =
        GetSizeCameraHelper.getSize(window, cameraSize)

    val finalHeight = size[0].toInt()
    val finalTop = size[1]

    val scaleX = screenWidth.toFloat() / originalWidth
    val scaleY = (finalTop + screenHeight.toFloat()) / originalHeight
    val scale = max(scaleX, scaleY)

    val centeringOffsetX = (originalWidth * scale - screenWidth) / 2f

    val cropX = (centeringOffsetX / scale).toInt()
    val cropY = (finalTop / scale).toInt()
    val cropWidth = (finalWidth / scale).toInt()
    val cropHeight = (finalHeight / scale).toInt()

    val croppedBitmap = Bitmap.createBitmap(
        originalBitmap,
        cropX,
        cropY,
        cropWidth,
        cropHeight
    )

    return croppedBitmap.scale(finalWidth, finalHeight)
}

