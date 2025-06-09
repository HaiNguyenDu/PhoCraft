package com.example.phocraft.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

object DrawFilterHelper {
    fun getMatrix(
        width: Int,
        height: Int,
        sourceWidth: Int,
        sourceHeight: Int,
        isFrontCamera: Boolean,
    ): Matrix {
        val scaleX = width / sourceHeight.toFloat()
        val scaleY = height / sourceWidth.toFloat()
        return Matrix().apply {
            preScale(scaleX, scaleY)
            if (isFrontCamera) {
                postTranslate(-width / 2f, -height / 2f)
                postScale(-1f, 1f)
                postTranslate(width / 2f, height / 2f)
            }
        }
    }

    fun drawHeadFilter(
        face: Face,
        canvas: Canvas,
        boundingBox: RectF,
        matrix: Matrix,
        isFrontCamera: Boolean,
        filterBitmap: Bitmap,
    ) {
        val filterWidth = boundingBox.width() * 1.2f

        val filterHeight = filterWidth * filterBitmap.height / filterBitmap.width

        val filterX = boundingBox.centerX() - (filterWidth / 2f)

        val filterY = boundingBox.top - filterHeight


        val destinationRect =
            RectF(filterX, filterY, filterX + filterWidth, filterY + filterHeight)

        canvas.drawBitmap(filterBitmap, null, destinationRect, null)
    }

    fun drawCheekFilter(
        face: Face,
        canvas: Canvas,
        boundingBox: RectF,
        matrix: Matrix,
        isFrontCamera: Boolean,
        filterBitmap: Bitmap,
    ) {


        val leftCheekPos = face.getLandmark(FaceLandmark.LEFT_CHEEK)?.position ?: return
        val rightCheekPos = face.getLandmark(FaceLandmark.RIGHT_CHEEK)?.position ?: return

        val margin = boundingBox.width() * 0.15f
        val leftCheekVector = floatArrayOf(leftCheekPos.x, leftCheekPos.y)
        val rightCheekVector = floatArrayOf(rightCheekPos.x, rightCheekPos.y)


        matrix.mapPoints(leftCheekVector)
        matrix.mapPoints(rightCheekVector)


        val filterDisplayWidth = (boundingBox.width() * 0.2).toInt()


        val aspectRatio = filterBitmap.height.toFloat() / filterBitmap.width.toFloat()
        val filterDisplayHeight = filterDisplayWidth * aspectRatio


        val leftCheekX = if (isFrontCamera) {
            leftCheekVector[0] + margin
        } else {
            leftCheekVector[0] - margin
        }
        val leftCheekY = leftCheekVector[1]

        val rectLeft = RectF(
            leftCheekX - filterDisplayWidth / 2,
            leftCheekY - filterDisplayHeight / 2,
            leftCheekX + filterDisplayWidth / 2,
            leftCheekY + filterDisplayHeight / 2
        )
        canvas.drawBitmap(filterBitmap, null, rectLeft, null)


        val rightCheekX = if (isFrontCamera) {
            rightCheekVector[0] - margin
        } else {
            rightCheekVector[0] + margin
        }
        val rightCheekY = rightCheekVector[1]

        val rectRight = RectF(
            rightCheekX - filterDisplayWidth / 2,
            rightCheekY - filterDisplayHeight / 2,
            rightCheekX + filterDisplayWidth / 2,
            rightCheekY + filterDisplayHeight / 2
        )
        canvas.drawBitmap(filterBitmap, null, rectRight, null)
    }
}