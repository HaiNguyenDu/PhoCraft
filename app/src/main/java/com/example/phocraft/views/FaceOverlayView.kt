package com.example.phocraft.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.phocraft.enum.FilterMode
import com.example.phocraft.utils.DrawFilterHelper
import com.google.mlkit.vision.face.Face

class FaceOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var faces = mutableListOf<Face>()
    private var filterMode: FilterMode? = FilterMode.NONE
    private var sourceWidth = 0
    private var sourceHeight = 0

    private var isFrontCamera = false

    private var filterBitmap: Bitmap? = null

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

    fun updateFilter(filterMode: FilterMode, filterBitmap: Bitmap?) {
        this.filterMode = filterMode
        this.filterBitmap = filterBitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (filterBitmap == null) return
        if (faces.isEmpty() || sourceWidth == 0) return

        val matrix = DrawFilterHelper.getMatrix(
            width,
            height,
            sourceWidth,
            sourceHeight,
            isFrontCamera
        )
        val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
        if (face == null) return

        val boundingBox = RectF(face.boundingBox)
        matrix.mapRect(boundingBox)

        if (filterMode == FilterMode.HEAD) {
            DrawFilterHelper.drawHeadFilter(
                face,
                canvas,
                boundingBox,
                matrix,
                isFrontCamera,
                filterBitmap!!
            )
        } else if (filterMode == FilterMode.CHEEK) {
            DrawFilterHelper.drawCheekFilter(
                face,
                canvas,
                boundingBox,
                matrix,
                isFrontCamera,
                filterBitmap!!
            )
        }
    }

}