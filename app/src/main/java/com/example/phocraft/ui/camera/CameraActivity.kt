package com.example.phocraft.ui.camera

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityCameraBinding
import com.example.phocraft.enum.CameraSize
import com.example.phocraft.utils.imageProxyToBitmapSinglePlane

class CameraActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }
    private val viewModel: CameraViewModel by viewModels()
    private var imageCapture: ImageCapture? = null
    private var currentSize = CameraSize.S4_3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        startCamera()
        setUpUi()
        setUpOnClick()
    }

    private fun setUpUi() {
        binding.overlayView.setSize(window, currentSize)
        binding.btnCamera.setOnClickListener {
            takePhoto()
        }
    }

    private fun setUpOnClick() {
        binding.apply {
            btnArrow.setOnClickListener {
                val animation =
                    AnimationUtils.loadAnimation(this@CameraActivity, R.anim.slide_bottom_to_top)
                layoutBtn.startAnimation(animation)
                layoutBtn.visibility = View.VISIBLE
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {


                    val bitmap = imageProxyToBitmapSinglePlane(window, image, currentSize)

                    if (bitmap != null)
                        viewModel.saveImageToGallery(bitmap)
                    else
                        Toast.makeText(this@CameraActivity, "fail", Toast.LENGTH_SHORT).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Lỗi chụp ảnh", Toast.LENGTH_SHORT).show()
                    Log.e("CameraX", "Capture failed", exception)
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1440, 3200))
                .setCaptureMode(CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}