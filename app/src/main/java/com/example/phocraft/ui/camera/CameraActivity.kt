package com.example.phocraft.ui.camera

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityCameraBinding
import com.example.phocraft.enum.CameraSize
import com.example.phocraft.enum.FilterMode
import com.example.phocraft.enum.FlashState
import com.example.phocraft.enum.TimerState
import com.example.phocraft.model.CameraUiState
import com.example.phocraft.utils.FaceAnalyzer
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }
    private val viewModel: CameraViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        var oldState: CameraUiState? = null

        viewModel.uiState.observe(this) { newState ->
            val needsRebind =
                oldState?.cameraSelector != newState.cameraSelector

            renderUi(newState)

            if (needsRebind || cameraProvider == null) {
                bindCameraUseCases(newState)
            }

            oldState = newState
        }
    }

    private fun renderUi(state: CameraUiState) {
        imageCapture?.flashMode = when (state.flashState) {
            FlashState.ON -> ImageCapture.FLASH_MODE_ON
            FlashState.OFF -> ImageCapture.FLASH_MODE_OFF
            FlashState.AUTO -> ImageCapture.FLASH_MODE_AUTO
        }

        val flashIcon = when (state.flashState) {
            FlashState.ON -> R.drawable.ic_flash_on
            FlashState.OFF -> R.drawable.ic_flash_off
            FlashState.AUTO -> R.drawable.ic_flash_auto
        }
        Glide.with(this).load(flashIcon).into(binding.btnFlash)

        binding.overlayView.setGrid(state.gridState)
        val gridIcon = if (state.gridState) R.drawable.ic_grid_on else R.drawable.ic_grid_off
        Glide.with(this).load(gridIcon).into(binding.tvGrid)

        setTextBtnTimer(state.timerState)
        val timerIcon =
            if (state.timerState != TimerState.OFF) R.drawable.ic_clock_on else R.drawable.ic_clock_off
        Glide.with(this).load(timerIcon).into(binding.icBtnTimer)

        setTextBtnSize(state.cameraSize)
        binding.overlayView.setSize(window, state.cameraSize)
        binding.overlayView.drawTextCountDown(state.countdownValue?.toString())

        val cameraButtonIcon = if (state.isTakingPicture && state.timerState != TimerState.OFF) {
            R.drawable.ic_pause
        } else {
            R.drawable.ic_rounded_white
        }
        Glide.with(this).load(cameraButtonIcon).into(binding.btnCamera)
        setOtherButtonsVisibility(
            binding.root,
            listOf(
                binding.btnCamera,
                binding.previewView,
                binding.faceOverlayView,
                binding.overlayView
            ),
            if (state.isTakingPicture) View.GONE else View.VISIBLE
        )

        binding.faceOverlayView.updateFilter(state.filterMode, state.filterBitmap)
    }

    private fun setupClickListeners() {
        binding.btnCamera.setOnClickListener { viewModel.onTakePhoto { executeImageCapture() } }
        binding.btnFlash.setOnClickListener { viewModel.onFlashToggled() }
        binding.btnSwap.setOnClickListener { viewModel.onCameraSwapped() }
        binding.btnGrid.setOnClickListener { viewModel.onGridToggled() }

        binding.btnArrow.setOnClickListener { toggleMainControlsGroupVisibility() }
        binding.btnLight.setOnClickListener { toggleBrightnessControlsVisibility() }
        binding.btnSize.setOnClickListener { toggleSizeControlsVisibility() }
        binding.btnTimer.setOnClickListener { toggleTimerControlsVisibility() }
        binding.btnFilter.setOnClickListener { toggleFilterControlsVisibility() }

        setupSizeOptionClickListeners()
        setupTimerOptionClickListeners()
        setUpFilterOptionsClick()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            this.cameraProvider = cameraProviderFuture.get()
            viewModel.uiState.value?.let { bindCameraUseCases(it) }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(state: CameraUiState) {
        val cameraProvider = this.cameraProvider ?: return
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder().build()


        val isFront = state.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutor,
                    FaceAnalyzer(binding.faceOverlayView, isFront) {
                        viewModel.updateLatestFaces(it)
                    },
                )
            }

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                state.cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
            renderUi(state)
            setupBrightnessControls()
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }
    }

    private fun executeImageCapture() {
        val imageCaptureInstance = this.imageCapture ?: return
        val state = viewModel.uiState.value!!

        imageCaptureInstance.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        lifecycleScope.launch {
                            viewModel.drawThenCropBitmap(image, window, state.cameraSize)
                        }
                    }
                    viewModel.onCaptureFinished()

                }

                override fun onError(exception: ImageCaptureException) {
                    viewModel.onCaptureFinished()
                }
            }
        )
    }


    private fun setUpFilterOptionsClick() {
        binding.apply {
            optionFilterCheekRabbit.setOnClickListener {
                val bitmap =
                    BitmapFactory.decodeResource(resources, R.drawable.filter_rabbit_cheek)
                viewModel.onFilterSelected(FilterMode.CHEEK, bitmap)
                selectFilterMode(it)
            }
            optionFilterHeadRabbit.setOnClickListener {
                val bitmap =
                    BitmapFactory.decodeResource(resources, R.drawable.filter_rabbit)
                viewModel.onFilterSelected(FilterMode.HEAD, bitmap)
                selectFilterMode(it)
            }
            optionFilterNone.setOnClickListener {
                viewModel.onFilterSelected(FilterMode.NONE, null)
                selectFilterMode(it)
            }
        }
    }

    private fun setupTimerOptionClickListeners() {
        binding.apply {
            optionT3.setOnClickListener { viewModel.onTimerSelected(TimerState.T_3) }
            optionT5.setOnClickListener { viewModel.onTimerSelected(TimerState.T_5) }
            optionT10.setOnClickListener { viewModel.onTimerSelected(TimerState.T_10) }
            optionOff.setOnClickListener { viewModel.onTimerSelected(TimerState.OFF) }
        }
    }

    private fun setupSizeOptionClickListeners() {
        binding.apply {
            option43.setOnClickListener { viewModel.onSizeSelected(CameraSize.S4_3) }
            option169.setOnClickListener { viewModel.onSizeSelected(CameraSize.S16_9) }
            optionFull.setOnClickListener { viewModel.onSizeSelected(CameraSize.SFull) }
            option11.setOnClickListener { viewModel.onSizeSelected(CameraSize.S1_1) }
        }
    }

    private fun toggleMainControlsGroupVisibility() {
        val layout = binding.layoutBtn
        val isExpanding = layout.isGone
        val animationRes =
            if (isExpanding) R.anim.slide_bottom_to_top else R.anim.slide_top_to_bottom
        val animation = AnimationUtils.loadAnimation(this, animationRes)
        layout.startAnimation(animation)
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
    }

    private fun selectFilterMode(view: View) {
        for (i in binding.layoutFilter.children) {
            if (i == view) {
                i.setBackgroundResource(R.drawable.bg_filter_item)
            } else i.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun setTextBtnSize(currentSize: CameraSize) {
        val (text, selectedOptionView) = when (currentSize) {
            CameraSize.S4_3 -> getString(R.string._4_3) to binding.option43
            CameraSize.S16_9 -> getString(R.string._16_9) to binding.option169
            CameraSize.S1_1 -> getString(R.string._1_1) to binding.option11
            CameraSize.SFull -> getString(R.string.full) to binding.optionFull
        }
        binding.textCurrentSize.text = text
        binding.layoutSize.children.forEach { v ->
            if (v is TextView) {
                val colorRes =
                    if (v == selectedOptionView) R.color.yellow else R.color.white
                v.setTextColor(ContextCompat.getColor(this, colorRes))
            }
        }
    }

    private fun setTextBtnTimer(timerState: TimerState) {
        val selectedOptionView = when (timerState) {
            TimerState.T_3 -> binding.optionT3
            TimerState.OFF -> binding.optionOff
            TimerState.T_5 -> binding.optionT5
            TimerState.T_10 -> binding.optionT10
        }
        binding.layoutTimer.children.forEach { v ->
            if (v is TextView) {
                val colorRes =
                    if (v == selectedOptionView) R.color.yellow else R.color.white
                v.setTextColor(ContextCompat.getColor(this, colorRes))
            }
        }
    }

    private fun toggleTimerControlsVisibility() {
        val layout = binding.layoutTimer
        val isExpanding = layout.visibility != View.VISIBLE
        TransitionManager.beginDelayedTransition(
            binding.layoutBtn,
            AutoTransition().apply { duration = 200 })
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnTimer),
            if (isExpanding) View.GONE else View.VISIBLE
        )
    }

    private fun toggleFilterControlsVisibility() {
        val layout = binding.scrollLayoutFilter
        val isExpanding = layout.visibility != View.VISIBLE
        TransitionManager.beginDelayedTransition(
            binding.layoutBtn,
            AutoTransition().apply { duration = 200 })
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnFilter),
            if (isExpanding) View.GONE else View.VISIBLE
        )

    }

    private fun toggleSizeControlsVisibility() {
        val layout = binding.layoutSize
        val isExpanding = layout.visibility != View.VISIBLE
        TransitionManager.beginDelayedTransition(
            binding.layoutBtn,
            AutoTransition().apply { duration = 200 })
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnSize),
            if (isExpanding) View.GONE else View.VISIBLE
        )
    }

    private fun toggleBrightnessControlsVisibility() {
        val layout = binding.layoutSeekBar
        val isExpanding = layout.visibility != View.VISIBLE
        TransitionManager.beginDelayedTransition(
            binding.layoutBtn,
            AutoTransition().apply { duration = 200 })
        layout.visibility = if (isExpanding) View.VISIBLE else View.GONE
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.btnLightLayout),
            if (isExpanding) View.GONE else View.VISIBLE
        )
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
                        viewModel.updateSeekBarValue(seekBar?.progress)
                    }
                })
            }
        }
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

    override fun onResume() {
        super.onResume()
        camera?.let {
            val brightness = viewModel.uiState.value?.brightness ?: 0
            it.cameraControl.setExposureCompensationIndex(
                brightness
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
