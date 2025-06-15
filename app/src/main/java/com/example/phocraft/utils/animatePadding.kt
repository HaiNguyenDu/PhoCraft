package com.example.phocraft.utils

import android.animation.ValueAnimator

import android.view.View
import android.view.animation.DecelerateInterpolator

fun View.animateSize(
    targetWidthPx: Int,
    duration: Long = 500L
) {

    val startWidth = this.width

    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.duration = duration
    animator.interpolator = DecelerateInterpolator()

    animator.addUpdateListener { valueAnimator ->
        val fraction = valueAnimator.animatedValue as Float

        val params = this.layoutParams

        params.width = (startWidth + (targetWidthPx - startWidth) * fraction).toInt()
        this.layoutParams = params
    }
    animator.start()
    }
