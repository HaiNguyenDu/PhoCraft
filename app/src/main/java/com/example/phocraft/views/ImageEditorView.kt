package com.example.phocraft.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

class ImageEditorView(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
//
//    private val canvasMatrix = Matrix()
//
//    private lateinit var scaleGestureDetector: ScaleGestureDetector
//    private lateinit var gestureDetector: GestureDetector
//    private var snapBackAnimator: ValueAnimator? = null
//
//    private var currentScale = 1.0f
//
//    private var baseImageView: ImageView? = null
    var drawView: DrawView? = null
    private var isDrawingMode = false
//
//    companion object {
//        private const val MIN_SCALE = 0.8f
//        private const val MAX_SCALE = 2.0f
//    }
//
//    init {
////        setupGestureDetectors()
//        setWillNotDraw(false)
//    }
//
//    private fun setupGestureDetectors() {
//        scaleGestureDetector = ScaleGestureDetector(
//            context,
//            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
//                override fun onScale(detector: ScaleGestureDetector): Boolean {
//                    val currentMatrixScale = FloatArray(9).apply { canvasMatrix.getValues(this) }[Matrix.MSCALE_X]
//                    var scaleFactor = detector.scaleFactor
//
//                    if (currentMatrixScale * scaleFactor > MAX_SCALE) {
//                        scaleFactor = MAX_SCALE / currentMatrixScale
//                    } else if (currentMatrixScale * scaleFactor < MIN_SCALE) {
//                        scaleFactor = MIN_SCALE / currentMatrixScale
//                    }
//
//                    canvasMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
//                    return true
//                }
//            })
//
//        gestureDetector =
//            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
//                override fun onScroll(
//                    e1: MotionEvent?,
//                    e2: MotionEvent,
//                    distanceX: Float,
//                    distanceY: Float,
//                ): Boolean {
//                    val drawable = baseImageView?.drawable ?: return true
//                    if (width == 0 || height == 0) return true
//
//                    val displayRect = RectF()
//                    val imageRect = RectF(
//                        0f,
//                        0f,
//                        drawable.intrinsicWidth.toFloat(),
//                        drawable.intrinsicHeight.toFloat()
//                    )
//                    canvasMatrix.mapRect(displayRect, imageRect)
//
//                    var actualDistanceX = distanceX
//                    var actualDistanceY = distanceY
//
//                    if (displayRect.width() > width || displayRect.height() > height) {
//                        val overscrollLimitX = width * (2f / 3f)
//                        val overscrollLimitY = height * (2f / 3f)
//
//                        if (displayRect.width() > width) {
//                            if (distanceX > 0) {
//                                val nextLeft = displayRect.left - distanceX
//                                if (nextLeft < -overscrollLimitX) {
//                                    actualDistanceX = displayRect.left + overscrollLimitX
//                                }
//                            } else {
//                                val nextRight = displayRect.right - distanceX
//                                if (nextRight > width + overscrollLimitX) {
//                                    actualDistanceX = displayRect.right - (width + overscrollLimitX)
//                                }
//                            }
//                        }
//
//                        if (displayRect.height() > height) {
//                            if (distanceY > 0) {
//                                val nextTop = displayRect.top - distanceY
//                                if (nextTop < -overscrollLimitY) {
//                                    actualDistanceY = displayRect.top + overscrollLimitY
//                                }
//                            } else {
//                                val nextBottom = displayRect.bottom - distanceY
//                                if (nextBottom > height + overscrollLimitY) {
//                                    actualDistanceY =
//                                        displayRect.bottom - (height + overscrollLimitY)
//                                }
//                            }
//                        }
//                    } else {
//                        val dampFactor = 2.5f
//                        actualDistanceX /= dampFactor
//                        actualDistanceY /= dampFactor
//                    }
//
//                    canvasMatrix.postTranslate(-actualDistanceX, -actualDistanceY)
//                    return true
//
//
//                }
//            })
//    }
//
//    override fun dispatchDraw(canvas: Canvas) {
//        canvas.save()
//        canvas.concat(canvasMatrix)
//        super.dispatchDraw(canvas)
//        canvas.restore()
//    }
//
//    fun setImageBitmap(bitmap: Bitmap) {
//        baseImageView?.let { removeView(it) }
//
//        val imageView = ImageView(context).apply {
//            setImageBitmap(bitmap)
//            scaleType = ImageView.ScaleType.CENTER
//        }
//        baseImageView = imageView
//        addView(imageView, 0, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
//        resetZoom()
//    }
//
//    fun addSticker(stickerView: View) {
//        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//        addView(stickerView, params)
//    }
//
//    fun resetZoom() {
//        canvasMatrix.reset()
//        currentScale = 1.0f
//        invalidate()
//    }
//
////    override fun onTouchEvent(event: MotionEvent): Boolean {
////        if (isDrawingMode) {
////            return super.onTouchEvent(event)
////        }
////        if (event.action == MotionEvent.ACTION_DOWN) {
////            snapBackAnimator?.cancel()
////        }
////
////        scaleGestureDetector.onTouchEvent(event)
////        gestureDetector.onTouchEvent(event)
////
////        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
//////            handleSnapBack()
////        }
////
////        invalidate()
////        return true
////    }
//
////    private fun handleSnapBack() {
////        val drawable = baseImageView?.drawable ?: return
////        val imageRect =
////            RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
////        val displayRect = RectF()
////        canvasMatrix.mapRect(displayRect, imageRect)
////
////        var targetX = displayRect.left
////        var targetY = displayRect.top
////
////
////        if (displayRect.width() < width) {
////            targetX = (width - displayRect.width()) / 2f
////        }
////
////        if (displayRect.height() < height) {
////            targetY = (height - displayRect.height()) / 2f
////        }
////
////        val deltaX = targetX - displayRect.left
////        val deltaY = targetY - displayRect.top
////
////
////        if (kotlin.math.abs(deltaX) < 1f && kotlin.math.abs(deltaY) < 1f) {
////            return
////        }
////
////        // Hủy animation cũ nếu đang chạy
////        snapBackAnimator?.cancel()
////
////        snapBackAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
////            duration = 300
////            interpolator = DecelerateInterpolator()
////
////            var lastAnimatedFraction = 0f
////
////            addUpdateListener { animation ->
////                val currentFraction = animation.animatedValue as Float
////                val deltaFraction = currentFraction - lastAnimatedFraction
////
////                val incrementalTranslateX = deltaX * deltaFraction
////                val incrementalTranslateY = deltaY * deltaFraction
////
////                canvasMatrix.postTranslate(incrementalTranslateX, incrementalTranslateY)
////                invalidate()
////
////                lastAnimatedFraction = currentFraction
////            }
////        }
////        snapBackAnimator?.start()
////    }

    fun setDrawingMode(enabled: Boolean) {
        this.isDrawingMode = enabled

        if (enabled && drawView == null) {
            drawView = DrawView(context)
            addView(drawView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        }

//        drawView?.isDrawingMode = enabled
    }

}