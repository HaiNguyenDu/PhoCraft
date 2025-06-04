package com.example.phocraft.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
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

    private val paint = Paint().apply {
        color = context.getColor(R.color.black_opacity_0_2)
    }

    private var targetHeight = 0f
    private var top = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(0f, 0f, width.toFloat(), top, paint)

        canvas.drawRect(0f, top + targetHeight, width.toFloat(), height.toFloat(), paint)
    }

    fun setSize(window: Window, cameraSize: CameraSize) {
        val (newTargetHeight, newTop) = GetSizeCameraHelper.getSize(window, cameraSize)

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

