package com.example.phocraft.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.Window
import android.view.animation.DecelerateInterpolator
import com.example.phocraft.R
import com.example.phocraft.enum.CameraSize
import com.example.phocraft.utils.GetSizeCameraHelper

class FrameOverlayView(
    context: Context,
    attrs: AttributeSet?,
) : View(context, attrs) {
    private val textPaint = Paint().apply {
        color = context.getColor(R.color.white)
        textSize = 300f
        textAlign = Paint.Align.CENTER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = resources.getFont(R.font.quicksand_bold)
        }
    }

    private var textCountDown: String? = null
    private val paint = Paint().apply {
        color = context.getColor(R.color.black_opacity_0_2)
    }

    private val whitePaint = Paint().apply {
        color = context.getColor(R.color.white)
        strokeWidth = 3f
    }


    private var targetHeight = 0f
    private var top = 0f
    private var gridState = true
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (gridState)
            drawGrid(canvas)
        if (textCountDown != null) {
            val y = (top + targetHeight / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
            val x = width.toFloat() / 2
            canvas.drawText(textCountDown!!, x, y, textPaint)
        }

        canvas.drawRect(0f, 0f, width.toFloat(), top, paint)
        canvas.drawRect(0f, top + targetHeight, width.toFloat(), height.toFloat(), paint)
    }

    fun setGrid(state: Boolean) {
        gridState = state
        invalidate()
    }

    fun drawGrid(canvas: Canvas) {
        val listArray = floatArrayOf(
            width.toFloat() / 3, top, width.toFloat() / 3, top + targetHeight,
            width.toFloat() * 2 / 3, top, width.toFloat() * 2 / 3, top + targetHeight,
            0f, top + targetHeight / 3, width.toFloat(), top + targetHeight / 3,
            0f, top + targetHeight * 2 / 3, width.toFloat(), top + targetHeight * 2 / 3
        )
        canvas.drawLines(listArray, whitePaint)
    }


    fun drawTextCountDown(text: String? = null) {
        textCountDown = text
        invalidate()
    }

    fun setSize(window: Window, cameraSize: CameraSize) {
        val (newTargetHeight, newTop) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            GetSizeCameraHelper.getSize(window, cameraSize)
        } else {
            TODO("VERSION.SDK_INT < R")
        }

        val startTop = top
        val startHeight = targetHeight

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedValue as Float
                top = startTop + (newTop - startTop) * fraction
                targetHeight = startHeight + (newTargetHeight - startHeight) * fraction
                invalidate()
            }
        }

        animator.start()
    }
}

