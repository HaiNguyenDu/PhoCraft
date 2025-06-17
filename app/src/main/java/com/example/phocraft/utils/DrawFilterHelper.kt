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
        canvas: Canvas,
        boundingBox: RectF,
        filterBitmap: Bitmap,
    ) {
        val filterWidth = 800

        val filterHeight = filterWidth * filterBitmap.height / filterBitmap.width

        val filterX = boundingBox.centerX() - (filterWidth / 2f)

        val filterY = boundingBox.top - filterHeight

        val destinationRect =
            RectF(filterX, filterY, filterX + filterWidth, filterY + filterHeight)

        canvas.drawBitmap(filterBitmap, null, destinationRect, null)
    }

    fun drawCheekFilterResult(
        face: Face,
        canvas: Canvas,
        isFrontCamera: Boolean,
        filterBitmap: Bitmap,
    ) {

        val leftCheekPos = face.getLandmark(FaceLandmark.LEFT_CHEEK)?.position ?: return
        val rightCheekPos = face.getLandmark(FaceLandmark.RIGHT_CHEEK)?.position ?: return

        val margin = 30
        val leftCheekVector = floatArrayOf(leftCheekPos.x, leftCheekPos.y)
        val rightCheekVector = floatArrayOf(rightCheekPos.x, rightCheekPos.y)

        val filterDisplayWidth = 150

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

    fun drawCheekFilter(
        face: Face,
        canvas: Canvas,
        matrix: Matrix,
        isFrontCamera: Boolean,
        filterBitmap: Bitmap,
    ) {

        val leftCheekPos = face.getLandmark(FaceLandmark.LEFT_CHEEK)?.position ?: return
        val rightCheekPos = face.getLandmark(FaceLandmark.RIGHT_CHEEK)?.position ?: return

        val margin = 30
        val leftCheekVector = floatArrayOf(leftCheekPos.x, leftCheekPos.y)
        val rightCheekVector = floatArrayOf(rightCheekPos.x, rightCheekPos.y)

        matrix.mapPoints(leftCheekVector)
        matrix.mapPoints(rightCheekVector)


        val filterDisplayWidth = 150


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