package com.example.phocraft.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.withMatrix
import com.example.phocraft.R
import com.example.phocraft.enum.StickerActionMode
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class CustomTextView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private var isFocus = true
    private var textBitmap: Bitmap? = null
    private val textMatrix = Matrix()
    private var isMatrixInitialized = false
    private var currentMode = StickerActionMode.NONE
    private var lastX = 0f
    private var lastY = 0f
    private var outlineColor = Color.TRANSPARENT
    private var outlineWidth = 15f
    val textView: TextView = TextView(context).apply {
        text = "Enter Your Text"
        textSize = 30f
        setTextColor(Color.WHITE)
    }

    private val stickerCenterPoint = PointF()
    private var startDistance = 0f
    private var startAngle = 0f

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    fun setOutline(color: Int? = null, width: Float? = null) {
        color?.let {
            this.outlineColor = color
        }
        width?.let {
            this.outlineWidth = width
        }
        updateBitmapAndMatrix()
    }

    fun getStrokeWidth(): Float {
        return outlineWidth + 15f
    }

    private val deleteHandleBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_delete_sticker).scale(40, 40)
    private val resizeHandleBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.ic_resize_sticker).scale(40, 40)

    private val gestureDetector: GestureDetector
    var onDoubleTapListener: (() -> Unit)? = null

    init {
        gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    onDoubleTapListener?.invoke()
                    return true
                }
            })
        updateBitmapAndMatrix()
    }

    fun setText(text: String) {
        textView.text = text
        updateBitmapAndMatrix()
    }

    private val deleteHandleRect = RectF()
    private val resizeHandleRect = RectF()
    private val handleSize = 60f

    fun setIsFocus(isFocus: Boolean) {
        this.isFocus = isFocus
        invalidate()
    }

    fun setTextColor(color: Int) {
        if (textView.currentTextColor == color) return
        textView.setTextColor(color)
        updateBitmapAndMatrix()
    }

    fun setTextSize(sizeInPx: Float) {
        if (textView.textSize == sizeInPx) return
        textView.textSize = sizeInPx
        updateBitmapAndMatrix()
    }

    fun setFont(typeface: Typeface) {
        if (textView.typeface == typeface) return
        textView.typeface = typeface
        updateBitmapAndMatrix()
    }
    fun setTextSizeInSp(sizeInSp: Float) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInSp)
        updateBitmapAndMatrix()
    }

    fun getTextSizeInSp(): Float {
        return textView.textSize / resources.displayMetrics.scaledDensity
    }

    fun getStrokeColor(): Int = outlineColor
    var onDeleteListener: (() -> Unit)? = null
    var onFocusListener: ((CustomTextView) -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && !isMatrixInitialized) {
            setupInitialMatrix()
        }
    }


    private fun setupInitialMatrix() {
        val bmp = textBitmap ?: return
        if (width == 0 || height == 0) return

        val translateX = (width - bmp.width) / 2f
        val translateY = (height - bmp.height) / 2f

        textMatrix.reset()
        textMatrix.postTranslate(translateX, translateY)

        isMatrixInitialized = true
        invalidate()
    }

    private fun updateBitmapAndMatrix() {
        if (textView.layoutParams == null) {
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val oldBitmap = textBitmap
        textView.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        if (textView.measuredWidth == 0 || textView.measuredHeight == 0) return

        val newBitmap =
            createBitmap(textView.measuredWidth, textView.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)

        if (outlineWidth > 0) {
            val originalPaint = textView.paint
            val originalColor = textView.currentTextColor

            originalPaint.style = Paint.Style.STROKE
            originalPaint.strokeWidth = outlineWidth
            textView.setTextColor(outlineColor)
            textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)
            textView.draw(canvas)

            originalPaint.style = Paint.Style.FILL
            textView.setTextColor(originalColor)
            textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)
            textView.draw(canvas)
        } else {
            textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)
            textView.draw(canvas)
        }
        textBitmap = newBitmap
        if (oldBitmap == null) {
            isMatrixInitialized = false
            requestLayout()
        } else {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = textBitmap ?: return


        canvas.withMatrix(textMatrix) {
            drawBitmap(bmp, 0f, 0f, null)
            if (isFocus)
                drawRect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat(), borderPaint)
        }

        if (isFocus)
            drawHandles(canvas)

    }

    fun getTextColor(): Int = textView.currentTextColor
    private fun drawHandles(canvas: Canvas) {
        val bmp = textBitmap ?: return


        val points = floatArrayOf(
            0f, 0f,
            bmp.width.toFloat(), 0f,
            bmp.width.toFloat(), bmp.height.toFloat(),
            0f, bmp.height.toFloat()
        )


        textMatrix.mapPoints(points)

        val topLeftX = points[0]
        val topLeftY = points[1]
        val bottomRightX = points[4]
        val bottomRightY = points[5]


        canvas.drawBitmap(
            deleteHandleBitmap,
            topLeftX - deleteHandleBitmap.width / 2,
            topLeftY - deleteHandleBitmap.height / 2,
            null
        )
        deleteHandleRect.set(
            topLeftX - handleSize / 2,
            topLeftY - handleSize / 2,
            topLeftX + handleSize / 2,
            topLeftY + handleSize / 2
        )


        canvas.drawBitmap(
            resizeHandleBitmap,
            bottomRightX - resizeHandleBitmap.width / 2,
            bottomRightY - resizeHandleBitmap.height / 2,
            null
        )
        resizeHandleRect.set(
            bottomRightX - handleSize / 2,
            bottomRightY - handleSize / 2,
            bottomRightX + handleSize / 2,
            bottomRightY + handleSize / 2
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                when {
                    deleteHandleRect.contains(event.x, event.y) -> {
                        onDeleteListener?.invoke()
                        return true
                    }

                    resizeHandleRect.contains(event.x, event.y) -> {
                        currentMode = StickerActionMode.RESIZE_ROTATE
                        lastX = event.x
                        lastY = event.y
                        updateCenterPoint()
                        startDistance = getDistance(
                            stickerCenterPoint.x,
                            stickerCenterPoint.y,
                            event.x,
                            event.y
                        )
                        startAngle =
                            getAngle(stickerCenterPoint.x, stickerCenterPoint.y, event.x, event.y)
                    }

                    isInsideSticker(event) -> {
                        currentMode = StickerActionMode.DRAG
                        lastX = event.x
                        lastY = event.y
                        if (!isFocus) {
                            onFocusListener?.invoke(this)
                            return true
                        }
                    }

                    else -> {
                        return false
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (currentMode) {
                    StickerActionMode.DRAG -> {
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        textMatrix.postTranslate(dx, dy)
                        lastX = event.x
                        lastY = event.y
                    }

                    StickerActionMode.RESIZE_ROTATE -> {

                        val newDistance = getDistance(
                            stickerCenterPoint.x,
                            stickerCenterPoint.y,
                            event.x,
                            event.y
                        )
                        val scale = newDistance / startDistance


                        val newAngle =
                            getAngle(stickerCenterPoint.x, stickerCenterPoint.y, event.x, event.y)
                        val angle = newAngle - startAngle

                        textMatrix.postScale(
                            scale,
                            scale,
                            stickerCenterPoint.x,
                            stickerCenterPoint.y
                        )
                        textMatrix.postRotate(angle, stickerCenterPoint.x, stickerCenterPoint.y)

                        startDistance = newDistance
                        startAngle = newAngle
                    }

                    else -> {}
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentMode = StickerActionMode.NONE
            }
        }
        invalidate()
        return true
    }


    private fun updateCenterPoint() {
        val bmp = textBitmap ?: return
        val points = floatArrayOf(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        textMatrix.mapPoints(points)
        stickerCenterPoint.set((points[0] + points[2]) / 2, (points[1] + points[3]) / 2)
    }

    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
    }

    private fun isInsideSticker(event: MotionEvent): Boolean {
        val bmp = textBitmap ?: return false
        val invertedMatrix = Matrix()
        textMatrix.invert(invertedMatrix)
        val points = floatArrayOf(event.x, event.y)
        invertedMatrix.mapPoints(points)
        val x = points[0]
        val y = points[1]
        return x >= 0 && x <= bmp.width && y >= 0 && y <= bmp.height
    }
}