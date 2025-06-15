package com.example.phocraft.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.core.graphics.createBitmap

object ColorFilterManager {
  fun applyAdjustments(
        originalBitmap: Bitmap,
        brightness: Int,
        contrast: Float,
        saturation: Float,
        hue: Float
    ): Bitmap {
        val resultBitmap = createBitmap(originalBitmap.width, originalBitmap.height)
        val canvas = Canvas(resultBitmap)
        val paint = Paint()
        val combinedMatrix = ColorMatrix()
        val saturationMatrix = ColorMatrix().apply { setSaturation(saturation) }
        combinedMatrix.postConcat(saturationMatrix)
        val contrastMatrix = ColorMatrix().apply {
            val scale = contrast
            val translate = (-.5f * scale + .5f) * 255f
            set(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        combinedMatrix.postConcat(contrastMatrix)
        val brightnessMatrix = ColorMatrix().apply {
            val brightnessFloat = brightness.toFloat()
            set(
                floatArrayOf(
                    1f, 0f, 0f, 0f, brightnessFloat,
                    0f, 1f, 0f, 0f, brightnessFloat,
                    0f, 0f, 1f, 0f, brightnessFloat,
                    0f, 0f, 0f, 1f, 0f
                )
            )
        }
        combinedMatrix.postConcat(brightnessMatrix)
        val hueMatrix = ColorMatrix()
        hueMatrix.setRotate(2, hue)
        combinedMatrix.postConcat(hueMatrix)
        paint.colorFilter = ColorMatrixColorFilter(combinedMatrix)
        canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

        return resultBitmap
    }
}