package com.example.phocraft.utils

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import com.example.phocraft.views.FaceOverlayView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceAnalyzer(
    private val faceOverlayView: FaceOverlayView,
    private val isFrontCamera: Boolean,
) : Analyzer {
    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(currImage, imageProxy.imageInfo.rotationDegrees)

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                faceOverlayView.update(
                    faces,
                    image.width,
                    image.height,
                    isFrontCamera,
                )
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

}