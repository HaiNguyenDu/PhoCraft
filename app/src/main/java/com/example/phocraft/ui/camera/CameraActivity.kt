package com.example.phocraft.ui.camera

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
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
import com.example.phocraft.enum.FilterMode
import com.example.phocraft.enum.FlashState
import com.example.phocraft.enum.TimerState
import com.example.phocraft.utils.FaceAnalyzer
import com.example.phocraft.utils.imageProxyToBitmapSinglePlane
import com.example.phocraft.views.FaceOverlayView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }
    private val viewModel: CameraViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    private var currentSize = CameraSize.S4_3
    private var gridState = true
    private var flashState: FlashState = FlashState.OFF
    private var timerState: TimerState = TimerState.OFF
    private var currentBrightness: Int? = null
    private var countDownTimer: CountDownTimer? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceOverlayView: FaceOverlayView
    private var filterMode = FilterMode.NONE
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageAnalyzer: ImageAnalysis? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        startCamera()
        setupUi()
        setupClickListeners()
    }

    private fun setupUi() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.overlayView.setSize(window, currentSize)
        updateFlashButtonIcon()
        updateTimerButtonIcon()
        updateGridButtonIcon()
        setTextBtnSize()
    }

    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener { handleCameraClick() }
        binding.btnFlash.setOnClickListener { toggleFlash() }
        binding.btnSwap.setOnClickListener { swapCamera() }
        binding.btnArrow.setOnClickListener { toggleMainControlsGroupVisibility() }

        setupMainControlsGroupClickListeners()
        setupSizeOptionClickListeners()
        setupTimerOptionClickListeners()
        setUpFilterOptionsClick()
    }

    private fun setUpFilterOptionsClick() {
        binding.apply {
            optionFilterCheekRabbit.setOnClickListener {
                filterMode = FilterMode.CHEEK
                val filterBitmap =
                    BitmapFactory.decodeResource(resources, R.drawable.filter_rabbit_cheek)
                binding.faceOverlayView.updateFilter(filterMode, filterBitmap)
                selectFilterMode(optionFilterCheekRabbit)
            }
            optionFilterHeadRabbit.setOnClickListener {
                filterMode = FilterMode.HEAD
                val filterBitmap = BitmapFactory.decodeResource(resources, R.drawable.filter_rabbit)
                binding.faceOverlayView.updateFilter(filterMode, filterBitmap)
                selectFilterMode(optionFilterHeadRabbit)
            }
            optionFilterNone.setOnClickListener {
                filterMode = FilterMode.NONE
                val filterBitmap = null
                binding.faceOverlayView.updateFilter(filterMode, filterBitmap)
                selectFilterMode(optionFilterNone)
            }
        }
    }

    private fun toggleFlash() {
        flashState = when (flashState) {
            FlashState.OFF -> FlashState.AUTO
            FlashState.AUTO -> FlashState.ON
            FlashState.ON -> FlashState.OFF
        }
        updateFlashButtonIcon()
        updateImageCaptureFlashMode()
    }

    private fun updateFlashButtonIcon() {
        val iconRes = when (flashState) {
            FlashState.ON -> R.drawable.ic_flash_on
            FlashState.OFF -> R.drawable.ic_flash_off
            FlashState.AUTO -> R.drawable.ic_flash_auto
        }
        Glide.with(this).load(iconRes).into(binding.btnFlash)
    }

    private fun updateImageCaptureFlashMode() {
        imageCapture?.flashMode = when (flashState) {
            FlashState.ON -> ImageCapture.FLASH_MODE_ON
            FlashState.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashState.AUTO -> ImageCapture.FLASH_MODE_AUTO
        }
    }

    private fun handleCameraClick() {
        if (countDownTimer != null) {
            cancelCountdown()
        } else {
            prepareAndTakePhoto()
        }
    }

    private fun cancelCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
        binding.overlayView.drawTextCountDown(null)
        setOtherButtonsVisibility(
            binding.root,
            listOf(binding.btnCamera, binding.overlayView, binding.previewView),
            View.VISIBLE
        )
        Glide.with(this)
            .load(R.drawable.ic_rounded_white)
            .into(binding.btnCamera)
    }

    private fun setupMainControlsGroupClickListeners() {
        binding.apply {
            btnLight.setOnClickListener { toggleBrightnessControlsVisibility() }
            btnSize.setOnClickListener { toggleSizeControlsVisibility() }
            btnGrid.setOnClickListener { toggleGrid() }
            btnTimer.setOnClickListener { toggleTimerControlsVisibility() }
            btnFilter.setOnClickListener { toggleFilterControlsVisibility() }
        }
    }

    private fun toggleFilterControlsVisibility() {
        val layout = binding.scrollLayoutFilter
        val isExpanding = layout.visibility != View.VISIBLE
        val transition = AutoTransition().apply { duration = 200 }

        TransitionManager.beginDelayedTransition(binding.layoutBtn, transition)
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnFilter),
            if (isExpanding) View.GONE else View.VISIBLE
        )
    }

    private fun selectFilterMode(view: View) {
        for (i in binding.layoutFilter.children) {
            if (i == view) {
                i.setBackgroundResource(R.drawable.bg_filter_item)
            } else i.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setupSizeOptionClickListeners() {
        binding.apply {
            option43.setOnClickListener { selectCameraSize(CameraSize.S4_3) }
            option169.setOnClickListener { selectCameraSize(CameraSize.S16_9) }
            optionFull.setOnClickListener { selectCameraSize(CameraSize.SFull) }
            option11.setOnClickListener { selectCameraSize(CameraSize.S1_1) }
        }
    }

    private fun selectCameraSize(size: CameraSize) {
        currentSize = size
        toggleSizeControlsVisibility()
    }

    private fun setupTimerOptionClickListeners() {
        binding.apply {
            optionT3.setOnClickListener { selectTimerOption(TimerState.T_3) }
            optionT5.setOnClickListener { selectTimerOption(TimerState.T_5) }
            optionT10.setOnClickListener { selectTimerOption(TimerState.T_10) }
            optionOff.setOnClickListener { selectTimerOption(TimerState.OFF) }
        }
    }

    private fun selectTimerOption(state: TimerState) {
        timerState = state
        toggleTimerControlsVisibility()
    }

    private fun toggleMainControlsGroupVisibility() {
        val layout = binding.layoutBtn
        val isExpanding = layout.isGone
        val animationRes =
            if (isExpanding) R.anim.slide_bottom_to_top else R.anim.slide_top_to_bottom
        val arrowIconRes = if (isExpanding) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down

        val animation = AnimationUtils.loadAnimation(this, animationRes)
        layout.startAnimation(animation)
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        Glide.with(this).load(arrowIconRes).into(binding.icBtnArrow)
    }

    private fun toggleGrid() {
        gridState = !gridState
        binding.overlayView.setGrid(gridState)
        updateGridButtonIcon()
    }

    private fun updateGridButtonIcon() {
        val iconRes = if (gridState) R.drawable.ic_grid_on else R.drawable.ic_grid_off
        Glide.with(this).load(iconRes).into(binding.tvGrid)
    }

    private fun setTextBtnSize() {
        val (text, selectedOptionView) = when (currentSize) {
            CameraSize.S4_3 -> getString(R.string._4_3) to binding.option43
            CameraSize.S16_9 -> getString(R.string._16_9) to binding.option169
            CameraSize.S1_1 -> getString(R.string._1_1) to binding.option11
            CameraSize.SFull -> getString(R.string.full) to binding.optionFull
        }
        binding.textCurrentSize.text = text

        binding.layoutSize.children.forEach { view ->
            if (view is TextView) {
                val colorRes = if (view == selectedOptionView) R.color.yellow else R.color.white
                view.setTextColor(ContextCompat.getColor(this, colorRes))
            }
        }
    }

    private fun setTextBtnTimer() {
        val selectedOptionView = when (timerState) {
            TimerState.T_3 -> binding.optionT3
            TimerState.OFF -> binding.optionOff
            TimerState.T_5 -> binding.optionT5
            TimerState.T_10 -> binding.optionT10
        }

        binding.layoutTimer.children.forEach { view ->
            if (view is TextView) {
                val colorRes = if (view == selectedOptionView) R.color.yellow else R.color.white
                view.setTextColor(ContextCompat.getColor(this, colorRes))
            }
        }
        updateTimerButtonIcon()
    }

    private fun updateTimerButtonIcon() {
        val iconRes =
            if (timerState != TimerState.OFF) R.drawable.ic_clock_on else R.drawable.ic_clock_off
        Glide.with(this).load(iconRes).into(binding.icBtnTimer)
    }

    private fun toggleTimerControlsVisibility() {
        setTextBtnTimer()
        val layout = binding.layoutTimer
        val isExpanding = layout.visibility != View.VISIBLE
        val transition = AutoTransition().apply { duration = 200 }

        TransitionManager.beginDelayedTransition(binding.layoutBtn, transition)
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnTimer),
            if (isExpanding) View.GONE else View.VISIBLE
        )
    }

    private fun toggleSizeControlsVisibility() {
        setTextBtnSize()
        val layout = binding.layoutSize
        val isExpanding = layout.visibility != View.VISIBLE
        val transition = AutoTransition().apply { duration = 200 }

        TransitionManager.beginDelayedTransition(binding.layoutBtn, transition)
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnSize),
            if (isExpanding) View.GONE else View.VISIBLE
        )
        binding.overlayView.setSize(window, currentSize)
    }

    private fun toggleBrightnessControlsVisibility() {
        val layout = binding.layoutSeekBar
        val isExpanding = layout.visibility != View.VISIBLE
        val transition = AutoTransition().apply { duration = 200 }

        TransitionManager.beginDelayedTransition(binding.layoutBtn, transition)
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.btnLightLayout),
            if (isExpanding) View.GONE else View.VISIBLE
        )
    }

    private fun setOtherButtonsVisibility(
        parent: ViewGroup,
        excludedViews: List<View>,
        visibility: Int,
    ) {
        parent.children.forEach { childView ->
            if (childView !in excludedViews) {
                childView.visibility = visibility
            }
        }
    }

    private fun prepareAndTakePhoto() {
        val currentImageCapture = imageCapture ?: return

        var initialTimeMillis = when (timerState) {
            TimerState.T_3 -> 3000L
            TimerState.T_5 -> 5000L
            TimerState.T_10 -> 10000L
            TimerState.OFF -> 0L
        }

        if (timerState != TimerState.OFF) {
            var mutableTimeMillis = initialTimeMillis
            setOtherButtonsVisibility(
                binding.root,
                listOf(binding.overlayView, binding.previewView, binding.btnCamera),
                View.GONE
            )
            Glide.with(this).load(R.drawable.ic_pause).into(binding.btnCamera)

            countDownTimer = object : CountDownTimer(initialTimeMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.overlayView.drawTextCountDown((mutableTimeMillis / 1000).toString())
                    mutableTimeMillis -= 1000
                }

                override fun onFinish() {
                    executeImageCapture(currentImageCapture)
                }
            }.start()
        } else {
            executeImageCapture(currentImageCapture)
        }
    }

    private fun executeImageCapture(imageCaptureInstance: ImageCapture) {
        countDownTimer = null
        binding.overlayView.drawTextCountDown(null)

        imageCaptureInstance.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        imageProxyToBitmapSinglePlane(window, image, currentSize)
                    } else {
                        TODO("VERSION.SDK_INT < R")
                    }
                    image.close() // Important: Close the ImageProxy

                    if (bitmap != null) {
//                        viewModel.saveImageToGallery(bitmap)
                        Toast.makeText(this@CameraActivity, "Image saved", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@CameraActivity,
                            "Failed to process image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    resetUiAfterCaptureAttempt()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Image capture failed: ${exception.message}", exception)
                    Toast.makeText(
                        this@CameraActivity,
                        "Image capture failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    resetUiAfterCaptureAttempt()
                }
            }
        )
    }

    private fun resetUiAfterCaptureAttempt() {
        if (timerState != TimerState.OFF) {
            setOtherButtonsVisibility(
                binding.root,
                listOf(binding.overlayView, binding.previewView, binding.btnCamera),
                View.VISIBLE
            )
            Glide.with(this@CameraActivity)
                .load(R.drawable.ic_rounded_white)
                .into(binding.btnCamera)
        }
    }

    private fun swapCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA
        bindCameraUseCases()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupBrightnessControls() {
        camera?.let { cam ->
            val cameraControl = cam.cameraControl
            val cameraInfo = cam.cameraInfo
            val exposureState = cameraInfo.exposureState
            val exposureRange = exposureState.exposureCompensationRange

            if (exposureRange.lower == 0 && exposureRange.upper == 0) {
                binding.btnLight.visibility = View.GONE
                binding.layoutSeekBar.visibility = View.GONE
                return
            } else {
                binding.btnLight.visibility = View.VISIBLE
            }

            binding.seekBar.apply {
                max = exposureRange.upper
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    min = exposureRange.lower
                }
                progress = exposureState.exposureCompensationIndex
                binding.tvLight.text = progress.toString()

                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        if (fromUser) {
                            binding.tvLight.text = progress.toString()
                            if (exposureRange.contains(progress)) {
                                cameraControl.setExposureCompensationIndex(progress)
                            }
                        }
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        currentBrightness = seekBar?.progress
                    }
                })
            }
        }
    }

    private fun bindCameraUseCases() {
        val cameraProvider = this.cameraProvider ?: return
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        var width: Float? = null
        var height: Float? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = window.windowManager.currentWindowMetrics
            width = metrics.bounds.width().toFloat()
            height = metrics.bounds.height().toFloat()
        } else {
            null
        }

        imageCapture = ImageCapture.Builder()
            .setTargetResolution(Size(width?.toInt() ?: 1920, height?.toInt() ?: 1080))
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .also { builder ->
                val flashMode = when (flashState) {
                    FlashState.ON -> ImageCapture.FLASH_MODE_ON
                    FlashState.OFF -> ImageCapture.FLASH_MODE_OFF
                    FlashState.AUTO -> ImageCapture.FLASH_MODE_AUTO
                }
                builder.setFlashMode(flashMode)
            }
            .build()

        try {
            val isFront = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(
                        cameraExecutor,
                        FaceAnalyzer(binding.faceOverlayView, isFront)
                    )
                }
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture, imageAnalyzer
            )

            setupBrightnessControls()
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
            Toast.makeText(this, "Failed to start camera.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        currentBrightness?.let { brightnessValue ->
            camera?.cameraControl?.setExposureCompensationIndex(brightnessValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        cameraExecutor.shutdown()
    }
}