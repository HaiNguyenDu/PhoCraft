package com.example.phocraft.views // Thay đổi package cho phù hợp

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
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.scale
import androidx.core.graphics.withMatrix
import com.example.phocraft.R
import com.example.phocraft.enum.StickerActionMode
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class StickerView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {


    private var isFocus = true
    private var stickerBitmap: Bitmap? = null
    private val stickerMatrix = Matrix()
    private var isMatrixInitialized = false
    private var currentMode = StickerActionMode.NONE
    private var lastX = 0f
    private var lastY = 0f


    private val stickerCenterPoint = PointF()
    private var startDistance = 0f
    private var startAngle = 0f

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Viền nét đứt
    }

    private val deleteHandleBitmap: Bitmap
    private val resizeHandleBitmap: Bitmap

    init {
        deleteHandleBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.ic_delete_sticker).scale(40, 40)
        resizeHandleBitmap =
            BitmapFactory.decodeResource(resources, R.drawable.ic_resize_sticker).scale(40, 40)
    }

    private val deleteHandleRect = RectF()
    private val resizeHandleRect = RectF()
    private val handleSize = 60f

    fun setIsFocus(isFocus: Boolean) {
        this.isFocus = isFocus
        invalidate()
    }

    var onDeleteListener: (() -> Unit)? = null
    var onFocusListener: ((StickerView) -> Unit)? = null

    fun setBitmap(bitmap: Bitmap) {

        this.stickerBitmap = bitmap
        isMatrixInitialized = false
        requestLayout()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && !isMatrixInitialized) {
            setupInitialMatrix()
        }
    }

    private fun setupInitialMatrix() {
        val bmp = stickerBitmap ?: return
        if (width == 0 || height == 0) return

        val targetWidth = 80f * resources.displayMetrics.density
        val targetHeight = 80f * resources.displayMetrics.density

        val bmpWidth = bmp.width.toFloat()
        val bmpHeight = bmp.height.toFloat()


        val scaleX = targetWidth / bmpWidth
        val scaleY = targetHeight / bmpHeight
        val scale = min(scaleX, scaleY)


        val newBmpWidth = bmpWidth * scale
        val newBmpHeight = bmpHeight * scale
        val translateX = (width - newBmpWidth) / 2f
        val translateY = (height - newBmpHeight) / 2f


        stickerMatrix.reset()
        stickerMatrix.postScale(scale, scale)
        stickerMatrix.postTranslate(translateX, translateY)

        isMatrixInitialized = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = stickerBitmap ?: return


        canvas.withMatrix(stickerMatrix) {

            drawBitmap(bmp, 0f, 0f, null)
            if (isFocus)
                drawRect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat(), borderPaint)
        }

        if (isFocus)
            drawHandles(canvas)
    }

    private fun drawHandles(canvas: Canvas) {
        val bmp = stickerBitmap ?: return


        val points = floatArrayOf(
            0f, 0f,
            bmp.width.toFloat(), 0f,
            bmp.width.toFloat(), bmp.height.toFloat(),
            0f, bmp.height.toFloat()
        )


        stickerMatrix.mapPoints(points)

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
                        stickerMatrix.postTranslate(dx, dy)
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

                        stickerMatrix.postScale(
                            scale,
                            scale,
                            stickerCenterPoint.x,
                            stickerCenterPoint.y
                        )
                        stickerMatrix.postRotate(angle, stickerCenterPoint.x, stickerCenterPoint.y)

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
        val bmp = stickerBitmap ?: return
        val points = floatArrayOf(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        stickerMatrix.mapPoints(points)
        stickerCenterPoint.set((points[0] + points[2]) / 2, (points[1] + points[3]) / 2)
    }

    private fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
    }

    private fun isInsideSticker(event: MotionEvent): Boolean {
        val bmp = stickerBitmap ?: return false
        val invertedMatrix = Matrix()
        stickerMatrix.invert(invertedMatrix)
        val points = floatArrayOf(event.x, event.y)
        invertedMatrix.mapPoints(points)
        val x = points[0]
        val y = points[1]
        return x >= 0 && x <= bmp.width && y >= 0 && y <= bmp.height
    }
}