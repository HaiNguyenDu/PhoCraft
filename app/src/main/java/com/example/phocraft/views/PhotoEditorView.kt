package com.example.phocraft.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.children
import com.example.phocraft.model.TextState

class PhotoEditorView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val imageView: ImageView
    private val drawView: DrawView
    private val stickerContainer: FrameLayout
    private var textContainer: FrameLayout
    private var isDrawingMode = false
    private val listStickerCurrent = mutableListOf<StickerView>()
    private var currentText: CustomTextView? = null
    private var originalTextState: TextState? = null

    lateinit var onMainState: () -> Unit

    init {
        imageView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener {
                if (!isDrawingMode)
                    onClickItem()
            }
        }
        addView(imageView)

        stickerContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(stickerContainer)
        textContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(textContainer)
        drawView = DrawView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(drawView)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isDrawingMode) {
            return false
        }
        return super.onInterceptTouchEvent(ev)
    }

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

    fun setPenColor(newColor: Int) = drawView.setPenColor(newColor)
    fun setPenWidth(newWidth: Float) = drawView.setPenWidth(newWidth)
    fun setEraserMode(isEnabled: Boolean) = drawView.setEraserMode(isEnabled)
    fun undo() = drawView.undo()
    fun redo() = drawView.redo()
    fun clearCanvas() = drawView.clearCanvas()

    fun setImageBitmap(bitmap: Bitmap) {
        imageView.setImageBitmap(bitmap)
    }

    fun setDrawingMode(isEnabled: Boolean) {
        isDrawingMode = isEnabled
        drawView.isDrawingEnabled = isEnabled
        if (isEnabled) {
            onClickItem(null)
        }
    }

    fun addSticker(stickerBitmap: Bitmap) {
        val stickerView = StickerView(context).apply {

            setBitmap(stickerBitmap)

            onDeleteListener = {
                stickerContainer.removeView(this)
            }

            onFocusListener = {
                onClickItem(it)
            }
        }
        listStickerCurrent.add(stickerView)
        stickerContainer.addView(stickerView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        onClickItem(stickerView)
    }

    fun addText(onTextState: () -> Unit, setTextState: () -> Unit) {
        val customTextView = CustomTextView(context).apply {
            onFocusListener = {
                onClickItem(null, this)
                onTextState()
                setTextState()
                currentText = it
            }
            onDeleteListener = {
                textContainer.removeView(this)
                currentText = null
                onClickItem()
            }
            onDoubleTapListener = {
                DialogEditText.show(context, this.textView.text.toString(), this)
            }
        }
        textContainer.addView(customTextView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        currentText = customTextView
        onClickItem(null, customTextView)
    }

    fun saveText() {
        currentText = null
//        originalTextState = null
        onClickItem()
    }

    fun exitText() {
//        currentText?.restoreState(originalTextState!!)
    }

    fun saveSticker() {
        listStickerCurrent.clear()
        onClickItem()
    }

    fun setOutline(color: Int? = null, width: Float? = null) {
        currentText?.setOutline(color, width)
    }

    fun getStrokeWidth(): Int? {
        return currentText?.getStrokeWidth()?.toInt()
    }

    fun exitSticker() {
        for (i in listStickerCurrent) {
            stickerContainer.removeView(i)
        }
        listStickerCurrent.clear()
        onClickItem()
    }

    fun setTextSize(size: Float) {
        currentText?.setTextSize(size)
    }

    fun setTextColor(color: Int) {
        currentText?.setTextColor(color)
    }

    fun setFont(typeface: Typeface) {
        currentText?.setFont(typeface)
    }

    fun onClickItem(stickerView: StickerView? = null, textView: CustomTextView? = null) {
        for (i in stickerContainer.children) {
            if (i as StickerView != stickerView)
                i.setIsFocus(false)
            else stickerView.setIsFocus(true)
        }
        for (i in textContainer.children) {
            if (i as CustomTextView != textView) {
                i.setIsFocus(false)
            } else textView.setIsFocus(true)
        }
        if (textView == null) {
            if (!isDrawingMode && stickerView == null)
                onMainState()
            currentText = null
        } else currentText = textView
    }
}