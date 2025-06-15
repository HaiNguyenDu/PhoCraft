package com.example.phocraft.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.phocraft.R

class DrawView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val undoStack = mutableListOf<Stroke>()
    private val redoStack = mutableListOf<Stroke>()
    private val currentPath = Path()
    var isDrawingEnabled: Boolean = false

    private val penPaint = Paint().apply {
        color = context.getColor(R.color.white)
        isAntiAlias = true
        strokeWidth = 20f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val eraserPaint = Paint().apply {
        strokeWidth = 50f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var currentPaint: Paint = penPaint

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun getPaintColor(): Int = penPaint.color
    fun setPenColor(newColor: Int) {
        penPaint.color = newColor
        currentPaint = penPaint
    }

    fun setPenWidth(newWidth: Float) {
        penPaint.strokeWidth = newWidth
        eraserPaint.strokeWidth = newWidth
    }

    fun setEraserMode(isEnabled: Boolean) {
        currentPaint = if (isEnabled) eraserPaint else penPaint
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val lastStroke = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(lastStroke)
            invalidate() // Yêu cầu vẽ lại View
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val lastStroke = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(lastStroke)
            invalidate()
        }
    }

    fun clearCanvas() {
        undoStack.clear()
        redoStack.clear()
        currentPath.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (stroke in undoStack) {
            canvas.drawPath(stroke.path, stroke.paint)
        }
        canvas.drawPath(currentPath, currentPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawingEnabled) return false
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                redoStack.clear()
                currentPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                currentPath.lineTo(x, y)
            }

            MotionEvent.ACTION_UP -> {
                undoStack.add(Stroke(Path(currentPath), Paint(currentPaint)))
                currentPath.reset()
            }

            else -> return false
        }

        invalidate()
        return true
    }

    data class Stroke(val path: Path, val paint: Paint)
}
