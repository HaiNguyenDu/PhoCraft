package com.example.phocraft.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.example.phocraft.R
import kotlin.math.min

class DrawView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val path = Path()
    private var isEraser = false
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private val undoStacks = mutableListOf<Stroke>()
    private val redoStacks = mutableListOf<Stroke>()
    private var baseImage: Bitmap? = null

    private var imageRectLeft: Float = 0f
    private var imageRectTop: Float = 0f
    private var imageRectRight: Float = 0f
    private var imageRectBottom: Float = 0f

    var isDrawingMode = false

    private val colorPaint = Paint().apply {
        color = context.getColor(R.color.black)
        strokeWidth = 10f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val borderPaint = Paint().apply {
        color = context.getColor(R.color.black)
        strokeWidth = 10f
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val eraserPaintPreview = Paint().apply {
        color = context.getColor(R.color.white)
        strokeWidth = 40f
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val eraserPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 40f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private lateinit var currentPaint: Paint

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        currentPaint = if (!isEraser) colorPaint else eraserPaintPreview
        rebuildBitmap()
    }

    fun getBitmapFromView(): Bitmap {
        val resultBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val resultCanvas = Canvas(resultBitmap)

        resultCanvas.drawColor(context.getColor(R.color.white))
        resultCanvas.drawBitmap(bitmap, 0f, 0f, null)
        resultCanvas.drawPath(path, currentPaint)

        if (baseImage != null) {
            resultCanvas.drawRect(
                imageRectLeft - borderPaint.strokeWidth / 2,
                imageRectTop - borderPaint.strokeWidth / 2,
                imageRectRight + borderPaint.strokeWidth / 2,
                imageRectBottom + borderPaint.strokeWidth / 2,
                borderPaint
            )
        } else {
            resultCanvas.drawRect(
                borderPaint.strokeWidth / 2,
                borderPaint.strokeWidth / 2,
                width - borderPaint.strokeWidth / 2,
                height - borderPaint.strokeWidth / 2,
                borderPaint
            )
        }
        return resultBitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.drawPath(path, currentPaint)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        clearCanvas()
        baseImage = bitmap

        val scaleX = width.toFloat() / bitmap.width.toFloat()
        val scaleY = height.toFloat() / bitmap.height.toFloat()
        val scaleFactor = min(scaleX, scaleY)

        val scaledWidth = (bitmap.width * scaleFactor).toInt()
        val scaledHeight = (bitmap.height * scaleFactor).toInt()

        val scaledBitmap = bitmap.scale(scaledWidth, scaledHeight)

        this.bitmap.eraseColor(Color.TRANSPARENT)

        val left = (width - scaledBitmap.width) / 2f
        val top = (height - scaledBitmap.height) / 2f

        imageRectLeft = left
        imageRectTop = top
        imageRectRight = left + scaledBitmap.width
        imageRectBottom = top + scaledBitmap.height

        canvas.drawBitmap(scaledBitmap, left, top, null)

        drawBorder()
        invalidate()
    }


    fun setEraser(state: Boolean) {
        isEraser = state
        currentPaint = if (isEraser) eraserPaintPreview else colorPaint
    }


    fun setPaintColor(color: Int) {
        colorPaint.color = color
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingMode) {
            return super.onTouchEvent(event)
        }
        val x = event.x
        val y = event.y

        if (baseImage != null) {
            if (x < imageRectLeft || x > imageRectRight || y < imageRectTop || y > imageRectBottom) {
                return true
            }
        } else {
            if (x < 0 || x > width || y < 0 || y > height) {
                return true
            }
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                val pathCopy = Path(path)

                val paintCopy = Paint(if (isEraser) eraserPaint else colorPaint)
                undoStacks.add(Stroke(pathCopy, paintCopy))
                canvas.drawPath(
                    path,
                    if (isEraser) eraserPaint else colorPaint
                )
                redoStacks.clear()
                path.reset()
            }
        }
        invalidate()
        return true
    }

    fun rebuildBitmap() {
        bitmap.eraseColor(Color.TRANSPARENT)

        baseImage?.let {
            val scaleX = width.toFloat() / it.width.toFloat()
            val scaleY = height.toFloat() / it.height.toFloat()
            val scaleFactor = min(scaleX, scaleY)

            val scaledWidth = (it.width * scaleFactor).toInt()
            val scaledHeight = (it.height * scaleFactor).toInt()
            val scaledBitmap = it.scale(scaledWidth, scaledHeight)

            val left = (width - scaledBitmap.width) / 2f
            val top = (height - scaledBitmap.height) / 2f

            imageRectLeft = left
            imageRectTop = top
            imageRectRight = left + scaledBitmap.width
            imageRectBottom = top + scaledBitmap.height

            canvas.drawBitmap(scaledBitmap, left, top, null)
        } ?: run {
            imageRectLeft = 0f
            imageRectTop = 0f
            imageRectRight = width.toFloat()
            imageRectBottom = height.toFloat()
        }

        for (stroke in undoStacks) {
            canvas.drawPath(stroke.path, stroke.paint)
        }
        invalidate()
    }


    fun setBorderColor(color: Int) {
        borderPaint.color = color
        drawBorder()
        invalidate()
    }

    private fun drawBorder() {
        if (baseImage != null) {
            canvas.drawRect(
                imageRectLeft + borderPaint.strokeWidth / 2,
                imageRectTop + borderPaint.strokeWidth / 2,
                imageRectRight - borderPaint.strokeWidth / 2,
                imageRectBottom - borderPaint.strokeWidth / 2,
                borderPaint
            )
        } else {
            canvas.drawRect(
                borderPaint.strokeWidth / 2,
                borderPaint.strokeWidth / 2,
                width - borderPaint.strokeWidth / 2,
                height - borderPaint.strokeWidth / 2,
                borderPaint
            )
        }
    }


    fun undo() {
        if (undoStacks.isEmpty()) return
        val last = undoStacks.removeAt(undoStacks.lastIndex)
        redoStacks.add(last)
        rebuildBitmap()
    }

    fun redo() {
        if (redoStacks.isEmpty()) return
        val last = redoStacks.removeAt(redoStacks.lastIndex)
        undoStacks.add(last)
        canvas.drawPath(last.path, last.paint)
        invalidate()
    }

    fun clearCanvas() {
        undoStacks.clear()
        redoStacks.clear()
        path.reset()
        baseImage = null
        bitmap.eraseColor(Color.TRANSPARENT)

        imageRectLeft = 0f
        imageRectTop = 0f
        imageRectRight = width.toFloat()
        imageRectBottom = height.toFloat()
        drawBorder()
        invalidate()
    }

    data class Stroke(val path: Path, val paint: Paint)
}
