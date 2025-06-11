package com.example.phocraft.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.children

class PhotoEditorView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val imageView: ImageView
    private val drawView: DrawView
    private val stickerContainer: FrameLayout
    private var isDrawingMode = false
    private val listStickerCurrent = mutableListOf<StickerView>()

    init {
        imageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener {
                onClickSticker(null)
            }
        }
        addView(imageView)

        stickerContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        addView(stickerContainer)
        drawView = DrawView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(drawView)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
    }

    fun setDrawingMode(isEnabled: Boolean) {
        isDrawingMode = isEnabled
        drawView.isDrawingEnabled = isEnabled
        if (isEnabled) {
            onClickSticker(null)
        }
    }

    fun setPenColor(newColor: Int) = drawView.setPenColor(newColor)
    fun setPenWidth(newWidth: Float) = drawView.setPenWidth(newWidth)
    fun setEraserMode(isEnabled: Boolean) = drawView.setEraserMode(isEnabled)
    fun undo() = drawView.undo()
    fun redo() = drawView.redo()
    fun clearCanvas() = drawView.clearCanvas()


    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        val drawable = imageView.drawable
        if (drawable != null) {
            val matrix = imageView.imageMatrix

            val imageRect =
                RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())

            matrix.mapRect(imageRect)

            canvas.clipRect(imageRect)
        }

        super.dispatchDraw(canvas)

        canvas.restore()
    }

    fun addSticker(stickerBitmap: Bitmap) {
        val stickerView = StickerView(context).apply {

            setBitmap(stickerBitmap)

            onDeleteListener = {
                stickerContainer.removeView(this)
            }

            onFocusListener = {
                onClickSticker(it)
            }
        }
        listStickerCurrent.add(stickerView)
        stickerContainer.addView(stickerView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        onClickSticker(stickerView)
    }

    fun saveSticker() {
        listStickerCurrent.clear()
        onClickSticker(null)
    }

    fun exitSticker() {
        for (i in listStickerCurrent) {
            stickerContainer.removeView(i)
        }
        listStickerCurrent.clear()
        onClickSticker(null)
    }

    fun onClickSticker(stickerView: StickerView?) {
        for (i in stickerContainer.children) {
            if (i as StickerView != stickerView)
                i.setIsFocus(false)
        }
        stickerView?.setIsFocus(true)
    }
}