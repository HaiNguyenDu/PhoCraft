package com.example.phocraft.ui.camera

import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.util.Size
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
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
    private var gridState = true
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var camera: Camera? = null
    private var currBrightness: Int? = 0
    private var flashState = false
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
            btnFlash.setOnClickListener {
                flashState = !flashState
                if (flashState) {
                    Glide.with(this@CameraActivity)
                        .load(R.drawable.ic_flash_on)
                        .into(btnFlash)
                } else
                    Glide.with(this@CameraActivity)
                        .load(R.drawable.ic_flash_off)
                        .into(btnFlash)
            }
            btnSwap.setOnClickListener {
                swapCameraSelector()
            }
            //btn arrow top
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
            //layoutbtn
            layoutBtn.apply {
                btnLight.setOnClickListener {
                    btnLightClick()
                }
                btnSizeMain.setOnClickListener {
                    toggleSizeOptions()
                }
                btnGrid.setOnClickListener {
                    gridState = !gridState
                    binding.overlayView.setGrid(gridState)
                    if (gridState) {
                        Glide.with(this@CameraActivity)
                            .load(R.drawable.ic_grid_on)
                            .into(tvGrid)
                    } else Glide.with(this@CameraActivity)
                        .load(R.drawable.ic_grid_off)
                        .into(tvGrid)
                }
            }

            //list button size
            btnSize.apply {
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
            var textView: TextView? = null
            when (currentSize) {
                CameraSize.S4_3 -> {
                    textCurrentSize.text = getString(R.string._4_3)
                    textView = binding.option43
                }

                CameraSize.S16_9 -> {
                    textCurrentSize.text = getString(R.string._16_9)
                    textView = binding.option169
                }

                CameraSize.S1_1 -> {
                    textCurrentSize.text = getString(R.string._1_1)
                    textView = binding.option11
                }

                CameraSize.SFull -> {
                    textCurrentSize.text = getString(R.string.full)
                    textView = binding.optionFull
                }
            }
            binding.layoutSize.children.forEach {
                if (it is TextView) {
                    if (it == textView)
                        it.setTextColor(getColor(R.color.yellow))
                    else
                        it.setTextColor(getColor(R.color.white))
                }
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

    private fun btnLightClick() {
        val isBtnOptionsExpanded = binding.layoutSeekBar.visibility != View.VISIBLE
        val transition = AutoTransition().apply {
            duration = 200
        }
        TransitionManager.beginDelayedTransition(binding.layoutBtn, transition)

        if (isBtnOptionsExpanded) {
            binding.layoutSeekBar.visibility = View.VISIBLE
            setOtherButtonsVisibility(binding.btnLightLayout, View.GONE)
        } else {
            binding.layoutSeekBar.visibility = View.GONE
            setOtherButtonsVisibility(binding.btnLightLayout, View.VISIBLE)
        }
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


                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        imageProxyToBitmapSinglePlane(window, image, currentSize)
                    } else {
                        null
                    }

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

    private fun swapCameraSelector() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        startCamera()
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

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

                setupBrightnessControls()
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupBrightnessControls() {
        camera?.let { cam ->
            val cameraControl = cam.cameraControl
            val cameraInfo = cam.cameraInfo

            val exposureState = cameraInfo.exposureState
            val exposureRange = exposureState.exposureCompensationRange

            binding.seekBar.apply {
                max = exposureRange.upper
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    min = exposureRange.lower
                }
                progress = exposureState.exposureCompensationIndex
                binding.tvLight.text = progress.toString()
                setOnSeekBarChangeListener(object :
                    SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        if (fromUser) {
                            binding.tvLight.text = progress.toString()
                            if (exposureRange.contains(progress)) {
                                cameraControl.setExposureCompensationIndex(progress)
                                    .addListener(
                                        {},
                                        ContextCompat.getMainExecutor(this@CameraActivity)
                                    )
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        currBrightness = seekBar?.progress
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (camera != null && currBrightness != null) {
            camera!!.cameraControl.setExposureCompensationIndex(currBrightness!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}