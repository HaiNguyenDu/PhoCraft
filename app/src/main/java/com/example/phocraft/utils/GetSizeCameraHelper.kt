package com.example.phocraft.utils

import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import com.example.phocraft.enum.CameraSize

object GetSizeCameraHelper {
    @RequiresApi(Build.VERSION_CODES.R)
    fun getSize(window: Window, cameraSize: CameraSize): List<Float> {
        val size = mutableListOf<Float>()
        val metrics = window.windowManager.currentWindowMetrics
        val width = metrics.bounds.width().toFloat()
        val height = metrics.bounds.height().toFloat()
        var top = height / 10
        when (cameraSize) {
            CameraSize.S1_1 -> {
                val targetHeight = width
                size.add(targetHeight)
                size.add(top)
            }

            CameraSize.S4_3 -> {
                val targetHeight = width * 4 / 3
                size.add(targetHeight)
                size.add(top)
            }

            CameraSize.S16_9 -> {
                val targetHeight = width.toFloat() * 16 / 9
                size.add(targetHeight)
                size.add(top)
            }

            CameraSize.SFull -> {
                top = 0f
                size.add(height)
                size.add(top)
            }
        }
        return size
    }
}