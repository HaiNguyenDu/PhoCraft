package com.example.phocraft.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withMatrix
import androidx.core.graphics.withSave
import androidx.core.view.children
import com.example.phocraft.enum.FilterType
import com.example.phocraft.model.FilterItem
import com.example.phocraft.model.PhotoAdjustments
import com.example.phocraft.utils.ColorFilterManager
import com.example.phocraft.utils.FilterManager

class PhotoEditorView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    private val imageView: ImageView
    private val drawView: DrawView
    private val stickerContainer: FrameLayout
    private var textContainer: FrameLayout
    private val frameOverlayView: ImageView
    private var currentFilterType: FilterType? = null
    private var focusedText: CustomTextView? = null
    private var isDrawingMode = false
    private var isFrameMode = false
    private var isFilterMode = false
    private var isAdjustmentMode = false
    private val listStickerCurrent = mutableListOf<StickerView>()

    private var originalBitmap: Bitmap? = null
    private var previousFrame: Bitmap? = null

    private var photoAdjustments: PhotoAdjustments = PhotoAdjustments()
    private var previousAdjustments: PhotoAdjustments = PhotoAdjustments()
    private var previousFilterType: FilterType? = null
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

        frameOverlayView = ImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        addView(frameOverlayView)
    }

    fun setFrame(frameBitmap: Bitmap?) {
        if (frameBitmap == null) {
            frameOverlayView.setImageDrawable(null)
            frameOverlayView.visibility = GONE
        } else {
            val drawable = imageView.drawable
            val fitBitmap =
                frameBitmap.scale(drawable.intrinsicWidth, drawable.intrinsicHeight, false)
            frameOverlayView.setImageBitmap(fitBitmap)
            frameOverlayView.visibility = VISIBLE
        }
    }

    fun setFilter(filterItem: FilterItem) {
        currentFilterType = filterItem.type
        val bitmapFilter = FilterManager.applyFilter(originalBitmap!!, filterItem.type)
        imageView.setImageBitmap(bitmapFilter)
    }

    fun getBitmap(): Bitmap {
        val drawable = imageView.drawable
        val originalImageWidth = drawable.intrinsicWidth
        val originalImageHeight = drawable.intrinsicHeight
        val resultBitmap =
            createBitmap(originalImageWidth, originalImageHeight)
        if (originalBitmap == null)
            return resultBitmap
        val canvas = Canvas(resultBitmap)
        drawable.draw(canvas)
        val inverseMatrix = Matrix()
        if (imageView.imageMatrix.invert(inverseMatrix)) {
            canvas.withMatrix(inverseMatrix) {
                stickerContainer.draw(this)
                frameOverlayView.draw(this)
                textContainer.draw(this)
                drawView.draw(this)
            }
        }

        return resultBitmap
    }

    fun getImageWidth(): Float {
        return imageView.drawable.intrinsicWidth.toFloat()
    }

    fun clearAllLayers() {
        drawView.clearCanvas()
        stickerContainer.removeAllViews()
        textContainer.removeAllViews()
        listStickerCurrent.clear()
        currentFilterType = null
        frameOverlayView.visibility = GONE
        previousFrame = null
        previousAdjustments = PhotoAdjustments()
        photoAdjustments = PhotoAdjustments()
        previousFilterType = null
        onClickItem()
        focusedText = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withSave() {
            val drawable = imageView.drawable
            if (drawable != null) {
                val matrix = imageView.imageMatrix

                val imageRect =
                    RectF(
                        0f,
                        0f,
                        drawable.intrinsicWidth.toFloat(),
                        drawable.intrinsicHeight.toFloat()
                    )

                matrix.mapRect(imageRect)

                clipRect(imageRect)
            }

            super.dispatchDraw(this)

        }
    }

    fun getPenColor(): Int? = drawView.getPaintColor()
    fun getTextColor(): Int? = focusedText?.getTextColor()
    fun getStrokeColor(): Int? = focusedText?.getStrokeColor()
    fun setPenColor(newColor: Int) = drawView.setPenColor(newColor)
    fun setPenWidth(newWidth: Float) = drawView.setPenWidth(newWidth)
    fun setEraserMode(isEnabled: Boolean) = drawView.setEraserMode(isEnabled)
    fun undo() = drawView.undo()
    fun redo() = drawView.redo()
    fun clearCanvas() = drawView.clearCanvas()
    fun getFilterType(): FilterType? = previousFilterType
    fun setImageBitmap(bitmap: Bitmap) {
        originalBitmap = bitmap
        if (previousFrame != null)
            imageView.setImageBitmap(bitmap)
        resetAdjustments()
    }

    private fun applyColorAdjustments(adjustments: PhotoAdjustments) {
        val bmp = originalBitmap ?: return

        val adjustedBitmap = ColorFilterManager.applyAdjustments(
            originalBitmap = bmp,
            brightness = adjustments.brightness,
            contrast = adjustments.contrast,
            saturation = adjustments.saturation,
            hue = adjustments.hue
        )
        imageView.setImageBitmap(adjustedBitmap)
    }

    fun setBrightness(value: Int) {
        this.photoAdjustments.brightness = value
        applyColorAdjustments(photoAdjustments)
    }

    fun setContrast(value: Float) {
        this.photoAdjustments.contrast = value
        applyColorAdjustments(photoAdjustments)
    }

    fun setSaturation(value: Float) {
        this.photoAdjustments.saturation = value
        applyColorAdjustments(photoAdjustments)
    }

    fun setHue(value: Float) {
        this.photoAdjustments.hue = value
        applyColorAdjustments(photoAdjustments)
    }

    fun getCurrentTextSizeInSp(): Float? {
        return focusedText?.getTextSizeInSp()
    }

    fun getAdjustment(): PhotoAdjustments {
        return photoAdjustments
    }

    fun resetAdjustments() {
        photoAdjustments = previousAdjustments.copy()
        applyColorAdjustments(photoAdjustments)
    }

    fun exitAdjustments() {
        photoAdjustments = previousAdjustments.copy()
        applyColorAdjustments(photoAdjustments)
    }

    fun saveAdjustments() {
        previousAdjustments = photoAdjustments.copy()

        val adjustedBitmap = ColorFilterManager.applyAdjustments(
            originalBitmap = originalBitmap!!,
            brightness = photoAdjustments.brightness,
            contrast = photoAdjustments.contrast,
            saturation = photoAdjustments.saturation,
            hue = photoAdjustments.hue
        )
        originalBitmap = adjustedBitmap
    }

    fun setDrawingMode(isEnabled: Boolean) {
        isDrawingMode = isEnabled
        drawView.isDrawingEnabled = isEnabled
        if (isEnabled) {
            onClickItem(null)
        }
    }

    fun saveFilter() {
        previousFilterType = currentFilterType
        currentFilterType = null
        originalBitmap = imageView.drawable.toBitmap()
    }

    fun exitFilter() {
        imageView.setImageBitmap(originalBitmap)
        currentFilterType = null
        isFilterMode = false
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
                focusedText = it
            }
            onDeleteListener = {
                textContainer.removeView(this)
                focusedText = null
                onClickItem()
            }
            onDoubleTapListener = {
                DialogEditText.show(context, this.textView.text.toString(), this)
            }
        }
        textContainer.addView(customTextView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        focusedText = customTextView
        onClickItem(null, customTextView)
    }

    fun saveText() {
        focusedText = null
        onClickItem()
    }

    fun saveSticker() {
        listStickerCurrent.clear()
        onClickItem()
    }

    fun setOutline(color: Int? = null, width: Float? = null) {
        focusedText?.setOutline(color, width)
    }

    fun getStrokeWidth(): Int? {
        return focusedText?.getStrokeWidth()?.toInt()
    }

    fun exitSticker() {
        for (i in listStickerCurrent) {
            stickerContainer.removeView(i)
        }
        listStickerCurrent.clear()
        onClickItem()
    }

    fun setTextSize(size: Float) {
        focusedText?.setTextSize(size)
    }

    fun setTextColor(color: Int) {
        focusedText?.setTextColor(color)
    }

    fun setFont(typeface: Typeface) {
        focusedText?.setFont(typeface)
    }

    fun exitFrame() {
        if (previousFrame != null) {
            frameOverlayView.setImageBitmap(previousFrame)
        } else {
            setFrame(null)
        }
    }

    fun saveFrame() {
        previousFrame = frameOverlayView.drawable.toBitmap()
    }

    fun setFrameMode(state: Boolean) {
        isFrameMode = state
    }

    fun setFilterMode(state: Boolean) {
        isFilterMode = state
    }

    fun setAdjustmentMode(state: Boolean) {
        isAdjustmentMode = state
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
            if (!isDrawingMode && !isFrameMode && !isFilterMode && !isAdjustmentMode && stickerView == null)
                onMainState()
            focusedText = null
        } else focusedText = textView
    }
}