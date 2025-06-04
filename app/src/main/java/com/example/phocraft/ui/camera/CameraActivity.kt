package com.example.phocraft.ui.camera

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
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
import androidx.core.view.children
import androidx.core.view.isGone
import com.bumptech.glide.Glide
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
                if (layoutBtn.isGone) {
                    val animation =
                        AnimationUtils.loadAnimation(
                            this@CameraActivity,
                            R.anim.slide_bottom_to_top
                        )
                    layoutBtn.startAnimation(animation)
                    layoutBtn.visibility = View.VISIBLE
                    Glide.with(this@CameraActivity)
                        .load(R.drawable.ic_arrow_up)
                        .into(icBtnArrow)
                } else {
                    val animation =
                        AnimationUtils.loadAnimation(
                            this@CameraActivity,
                            R.anim.slide_top_to_bottom
                        )
                    layoutBtn.startAnimation(animation)
                    layoutBtn.visibility = View.GONE
                    Glide.with(this@CameraActivity)
                        .load(R.drawable.ic_arrow_down)
                        .into(icBtnArrow)
                }
            }

            layoutBtn.apply {
                btnSizeMain.setOnClickListener {
                    toggleSizeOptions()
                }
                option43.setOnClickListener {
                    currentSize = CameraSize.S4_3
                    toggleSizeOptions()
                }
                option169.setOnClickListener {

                    currentSize = CameraSize.S16_9
                    toggleSizeOptions()
                }
                optionFull.setOnClickListener {
                    currentSize = CameraSize.SFull
                    toggleSizeOptions()
                }
                option11.setOnClickListener {
                    currentSize = CameraSize.S1_1
                    toggleSizeOptions()
                }
            }
        }
    }

    private fun setTextBtnSize() {
        binding.apply {
            when (currentSize) {
                CameraSize.S4_3 -> textCurrentSize.text = getString(R.string._4_3)
                CameraSize.S16_9 -> textCurrentSize.text = getString(R.string._16_9)
                CameraSize.S1_1 -> textCurrentSize.text = getString(R.string._1_1)
                CameraSize.SFull -> textCurrentSize.text = getString(R.string.full)
            }
        }
    }

    private fun toggleSizeOptions() {
        setTextBtnSize()
        val isSizeOptionsExpanded = binding.layoutSize.visibility != View.VISIBLE
        val transition = AutoTransition().apply {
            duration = 200
        }
        TransitionManager.beginDelayedTransition(binding.layoutBtn, transition)

        if (isSizeOptionsExpanded) {
            binding.layoutSize.visibility = View.VISIBLE
            setOtherButtonsVisibility(binding.btnSize, View.GONE)
        } else {
            binding.layoutSize.visibility = View.GONE
            setOtherButtonsVisibility(binding.btnSize, View.VISIBLE)
        }
        binding.overlayView.setSize(window, currentSize)
    }

    private fun setOtherButtonsVisibility(currBtn: View, visibility: Int) {
        binding.layoutBtn.children.forEach { childView ->
            if (childView != currBtn)
                childView.visibility = visibility
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