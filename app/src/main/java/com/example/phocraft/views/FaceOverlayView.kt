package com.example.phocraft.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.phocraft.R
import com.google.mlkit.vision.face.Face

class FaceOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val faces = mutableListOf<Face>()

    private var sourceWidth = 0
    private var sourceHeight = 0

    private var isFrontCamera = false

    private val filterBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.filter_rabbit)

    private val debugPaint = Paint().apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
    }

    fun update(
        faces: List<Face>,
        sourceWidth: Int,
        sourceHeight: Int,
        isFrontCamera: Boolean,
    ) {
        this.faces.clear()
        this.faces.addAll(faces)
        this.sourceWidth = sourceWidth
        this.sourceHeight = sourceHeight
        this.isFrontCamera = isFrontCamera

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (faces.isEmpty() || sourceWidth == 0) return

        val scaleX = width.toFloat() / sourceHeight.toFloat()
        val scaleY = height.toFloat() / sourceWidth.toFloat()

        val matrix = Matrix().apply {
            preScale(scaleX, scaleY)
            if (isFrontCamera) {
                postTranslate(-width / 2f, -height / 2f)
                postScale(-1f, 1f)
                postTranslate(width / 2f, height / 2f)
            }
        }

        for (face in faces) {
            val boundingBox = RectF(face.boundingBox)

            matrix.mapRect(boundingBox)

            val filterWidth = boundingBox.width() * 0.8f

            val filterHeight = filterWidth * filterBitmap.height / filterBitmap.width

            val filterX = boundingBox.centerX() - (filterWidth / 2f)

            val filterY = boundingBox.top - (filterHeight / 2f)


            val destinationRect = RectF(filterX, filterY, filterX + filterWidth, filterY + filterHeight)


            canvas.drawBitmap(filterBitmap, null, destinationRect, null)
        }
    }
}