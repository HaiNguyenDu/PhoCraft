package com.example.phocraft.utils

import android.util.DisplayMetrics
import android.view.Window
import com.example.phocraft.enum.CameraSize

object GetSizeCameraHelper {
    fun getSize(window: Window, cameraSize: CameraSize): List<Float> {
        val size = mutableListOf<Float>()
        val metrics = DisplayMetrics().also { window.windowManager.defaultDisplay.getMetrics(it) }
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()
        when (cameraSize) {
            CameraSize.S1_1 -> {
                val targetHeight = width
                val top = ((height - targetHeight) / 2) - ((height - targetHeight) / 4)
                size.add(targetHeight)
                size.add(top)
            }

            CameraSize.S4_3 -> {
                val targetHeight = width * 4 / 3
                val top = ((height - targetHeight) / 2) - ((height - targetHeight) / 4)
                size.add(targetHeight)
                size.add(top)
            }

            CameraSize.S16_9 -> {
                val targetHeight = width.toFloat() * 16 / 9
                val top = height - targetHeight
                size.add(targetHeight)
                size.add(top)
            }

            CameraSize.SFull -> {
                size.add(height)
                size.add(0f)
            }
        }
        return size
    }
}