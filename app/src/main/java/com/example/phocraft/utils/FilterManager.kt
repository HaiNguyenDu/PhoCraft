package com.example.phocraft.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import androidx.core.graphics.createBitmap
import com.example.phocraft.enum.FilterType
import kotlin.random.Random

object FilterManager {
    fun applyFilter(originalBitmap: Bitmap, filterType: FilterType): Bitmap {
        if (originalBitmap.width <= 0 || originalBitmap.height <= 0) {
            return originalBitmap
        }

        return when (filterType) {
            FilterType.NONE -> originalBitmap
            FilterType.SEPIA -> applySepiaFilter(originalBitmap)
            FilterType.INVERT -> applyInvertFilter(originalBitmap)
            FilterType.NOISE -> applyNoiseFilter(originalBitmap)
            FilterType.GRAYSCALE -> applyGrayscaleFilter(originalBitmap)
        }
    }

    private fun applySepiaFilter(original: Bitmap): Bitmap {
        val result = createBitmap(original.width, original.height)
        val canvas = Canvas(result)
        val paint = Paint()
        val matrix = ColorMatrix().apply {
            setSaturation(0f)
            val sepiaMatrix = ColorMatrix().apply {
                setScale(1f, 0.95f, 0.82f, 1f)
            }
            postConcat(sepiaMatrix)
        }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    private fun applyInvertFilter(original: Bitmap): Bitmap {
        val result = createBitmap(original.width, original.height)
        val canvas = Canvas(result)
        val paint = Paint()
        val matrix = ColorMatrix(
            floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    private fun applyNoiseFilter(original: Bitmap, intensity: Int = 40): Bitmap {
        val width = original.width
        val height = original.height
        val pixels = IntArray(width * height)
        original.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val originalColor = pixels[i]
            val noise = Random.nextInt(-intensity, intensity)
            var r = (Color.red(originalColor) + noise).coerceIn(0, 255)
            var g = (Color.green(originalColor) + noise).coerceIn(0, 255)
            var b = (Color.blue(originalColor) + noise).coerceIn(0, 255)
            pixels[i] = Color.rgb(r, g, b)
        }
        val result = createBitmap(width, height)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    private fun applyGrayscaleFilter(original: Bitmap): Bitmap {
        val result = createBitmap(original.width, original.height)
        val canvas = Canvas(result)
        val paint = Paint()
        val matrix = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }
}