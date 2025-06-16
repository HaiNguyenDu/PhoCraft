package com.example.phocraft.ui.editor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityEditorBinding
import com.example.phocraft.enum.AdjustmentsState
import com.example.phocraft.enum.EditingUiState
import com.example.phocraft.ui.editor.adapter.ColorAdapter
import com.example.phocraft.ui.editor.adapter.FilterAdapter
import com.example.phocraft.ui.editor.adapter.FontAdapter
import com.example.phocraft.ui.editor.adapter.StickerAdapter
import com.example.phocraft.ui.home.HomeActivity
import com.example.phocraft.ui.save_screen.SaveActivity
import com.example.phocraft.utils.BitmapCacheManager
import com.example.phocraft.utils.CONVERT_PAINT_WIDTH
import com.example.phocraft.utils.CURRENT_PHOTO_KEY
import com.example.phocraft.utils.PREVIOUS_PHOTO_KEY
import com.example.phocraft.utils.SCALE_FACTOR
import com.example.phocraft.views.ConfirmDialog
import kotlinx.coroutines.launch

class EditorActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditorBinding.inflate(layoutInflater) }
    private val viewModel: EditorViewModel by viewModels()

    var slideUpFadeIn: Animation? = null
    var slideUpFadeOut: Animation? = null

    private lateinit var colorAdapter: ColorAdapter
    private lateinit var stickerAdapter: StickerAdapter
    private lateinit var fontAdapter: FontAdapter
    private lateinit var frameAdapter: StickerAdapter
    private lateinit var filterAdapter: FilterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideSystemUI()
        setUpUi()
        setOnClick()
        observeLiveData()
        setUpSeekBar()
        setUpBackPressCallBack()
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION") window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    private fun setUpUi() {
        val currentPhoto = BitmapCacheManager.getBitmapFromMemCache(CURRENT_PHOTO_KEY)

        if (currentPhoto == null) {
            Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.imageEditorView.setImageBitmap(currentPhoto)
        binding.imageEditorView.onMainState = {
            onMainState()
        }
        slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        slideUpFadeOut = AnimationUtils.loadAnimation(this, R.anim.slide_down_fate_out)
        setupAdapters(currentPhoto)
    }

    private fun setupAdapters(bitmap: Bitmap) {
        colorAdapter = ColorAdapter(this, viewModel.listColor.value ?: emptyList()) { color ->
            handleColorSelection(color!!)
        }
        binding.rcvColor.adapter = colorAdapter
        binding.rcvColor.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        stickerAdapter = StickerAdapter(viewModel.stickers.value ?: emptyList()) { sticker ->
            binding.imageEditorView.addSticker(sticker)
        }
        binding.rcvSticker.adapter = stickerAdapter
        binding.rcvSticker.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        fontAdapter = FontAdapter(this, viewModel.listFont.value ?: emptyList()) { fontItem ->
            fontItem.typeface.let {
                binding.imageEditorView.setFont(it)
            }
        }
        binding.rcvFont.adapter = fontAdapter
        binding.rcvFont.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        frameAdapter = StickerAdapter(viewModel.listFrame.value ?: emptyList()) { frame ->
            binding.imageEditorView.setFrame(frame)
        }
        binding.rcvFrame.adapter = frameAdapter
        binding.rcvFrame.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewModel.setUpListFilter(bitmap)
        filterAdapter = FilterAdapter(
            viewModel.filters.value!!
        ) { filter ->
            binding.rcvFilter.children.forEach {

            }
            viewModel.viewModelScope.launch {
                viewModel.runWithLoading {
                    binding.imageEditorView.setFilter(filter)
                }
            }
        }
        binding.rcvFilter.adapter = filterAdapter
        binding.rcvFilter.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewModel.setUpListFilter(bitmap)
    }

    private fun handleColorSelection(color: Int) {
        when (viewModel.uiState.value) {
            EditingUiState.DRAW -> {
                binding.imageEditorView.setPenColor(color)
                viewModel.setEraser(false)
            }

            EditingUiState.TEXT -> {
                if (binding.layoutStrokeSize.isVisible) binding.imageEditorView.setOutline(color)
                else binding.imageEditorView.setTextColor(color)
            }

            else -> {

            }
        }
    }

    private fun setUpSeekBar() {
        binding.sizeSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean,
            ) {
                val width = p1 * CONVERT_PAINT_WIDTH
                binding.imageEditorView.setPenWidth(width.toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        binding.seekbarSizeStroke.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean,
            ) {
                val width = p1.toFloat() - 15f
                binding.imageEditorView.setOutline(null, width)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        binding.seekbarTextSize.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean,
            ) {
                binding.tvTextSize.text = p1.toString()
                binding.imageEditorView.setTextSize(p1.toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        binding.seekbarAdjustment.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean,
            ) {
                binding.tvTextAdjustments.text = (p1 - 100).toString()
                when (viewModel.adjustmentsState.value) {
                    AdjustmentsState.BRIGHTNESS -> {
                        val brightnessValue = p1 - 100
                        binding.imageEditorView.setBrightness(brightnessValue)
                    }

                    AdjustmentsState.HUE -> {
                        val hueValue = (p1 - 100) * 1.8f
                        binding.imageEditorView.setHue(hueValue)
                    }

                    AdjustmentsState.CONTRAST -> {
                        val contrastValue = p1.toFloat() / 100f
                        binding.imageEditorView.setContrast(contrastValue)
                    }

                    AdjustmentsState.SATURATION -> {
                        val saturationValue = p1.toFloat() / 100f
                        binding.imageEditorView.setSaturation(saturationValue)
                    }

                    else -> {
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
    }

    private fun observeLiveData() {

        viewModel.isLoading.observe(this) { isLoading ->
            binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.layoutBtn.isEnabled = !isLoading
        }
        viewModel.photo.observe(this) {
            binding.imageEditorView.setImageBitmap(it)
        }
        viewModel.uiState.observe(this) {
            when (it) {
                EditingUiState.MAIN -> onMainState()
                EditingUiState.DRAW -> onDrawState()
                EditingUiState.TEXT -> onTextState()
                EditingUiState.STICKER -> onStickerState()
                EditingUiState.CROP -> onCropState()
                EditingUiState.FRAME -> onFrameState()
                EditingUiState.Filter -> onFilterState()
                EditingUiState.ADJUSTMENTS -> onAdjustmentsState()
                else -> onMainState()
            }
        }
        viewModel.isEraser.observe(this) {
            binding.btnEraser.setBackgroundResource(if (it == true) R.drawable.bg_white_border else R.drawable.bg_no_border)
            binding.imageEditorView.setEraserMode(it)
        }
        viewModel.filters.observe(this) {
            filterAdapter.updateFilters(it)
        }
        viewModel.adjustmentsState.observe(this) {
            binding.apply {
                val adjustments = binding.imageEditorView.getAdjustment()
                when (it) {
                    AdjustmentsState.BRIGHTNESS -> {
                        setFocusBtnAdjustments(btnBrightness)
                        seekbarAdjustment.progress = adjustments.brightness + 100
                    }

                    AdjustmentsState.HUE -> {
                        setFocusBtnAdjustments(btnHue)
                        seekbarAdjustment.progress = ((adjustments.hue / 1.8f) + 100).toInt()
                    }

                    AdjustmentsState.CONTRAST -> {
                        setFocusBtnAdjustments(btnContrast)
                        seekbarAdjustment.progress = (adjustments.contrast * 100).toInt()
                    }

                    AdjustmentsState.SATURATION -> {
                        setFocusBtnAdjustments(btnSaturation)
                        seekbarAdjustment.progress = (adjustments.saturation * 100).toInt()
                    }

                    AdjustmentsState.NONE -> {
                        setFocusBtnAdjustments()
                        layoutAdjustment.visibility = View.GONE
                        return@observe
                    }

                    else -> {
                    }
                }
                if (layoutAdjustment.isGone) layoutAdjustment.fadeIn()
            }
        }
    }

    private fun setupAdjustmentControls() {
        binding.apply {
            btnBrightness.setOnClickListener {
                viewModel.setAdjustmentsState(AdjustmentsState.BRIGHTNESS)
            }
            btnContrast.setOnClickListener {
                viewModel.setAdjustmentsState(AdjustmentsState.CONTRAST)
            }
            btnHue.setOnClickListener {
                viewModel.setAdjustmentsState(AdjustmentsState.HUE)
            }
            btnSaturation.setOnClickListener {
                viewModel.setAdjustmentsState(AdjustmentsState.SATURATION)
            }
            btnReset.setOnClickListener {
                ConfirmDialog.show(this@EditorActivity) {
                    binding.imageEditorView.resetAdjustments()
                    viewModel.setAdjustmentsState(AdjustmentsState.NONE)
                }

            }
        }
    }

    private fun onAdjustmentsState() {
        setOtherButtonsVisibility(
            binding.layoutBtn, listOf(), View.GONE
        )
        binding.apply {
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
            layoutBtnAdjustments.fadeIn()
            btnRestore.visibility = View.GONE
        }
        binding.btnReset.fadeIn()
        viewModel.setAdjustmentsState(AdjustmentsState.NONE)
        binding.imageEditorView.setAdjustmentMode(true)
    }

    private fun onFilterState() {
        val currentFilterType = binding.imageEditorView.getFilterType()
        filterAdapter.setCurrentFilter(currentFilterType)
        setOtherButtonsVisibility(
            binding.layoutBtn, listOf(), View.GONE
        )
        binding.apply {
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
            btnRestore.visibility = View.GONE
            rcvFilter.fadeIn()
        }
    }

    private fun setFocusBtnAdjustments(view: View? = null) {
        binding.layoutBtnAdjustmentsLinear.children.forEach {
            if (it == view) it.setBackgroundResource(R.drawable.bg_white_border)
            else {
                it.setBackgroundResource(R.drawable.bg_no_border)
            }
        }
    }

    private fun onStickerState() {
        setOtherButtonsVisibility(
            binding.layoutBtn, listOf(), View.GONE
        )
        binding.apply {
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
            btnRestore.visibility = View.GONE
            rcvSticker.fadeIn()
        }
    }

    private fun onFrameState() {
        setOtherButtonsVisibility(
            binding.layoutBtn, listOf(), View.GONE
        )
        binding.apply {
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
            btnRestore.visibility = View.GONE
            rcvFrame.fadeIn()
        }
        binding.imageEditorView.setFrameMode(true)
    }

    private fun setOnClick() {
        binding.apply {
            btnText.setOnClickListener {
                viewModel.setUiState(EditingUiState.TEXT)
                imageEditorView.addText({
                    onTextState()
                    setFocusBtnText(null)
                }, {
                    viewModel.setUiState(EditingUiState.TEXT)
                })
            }
            btnArt.setOnClickListener {
                imageEditorView.setDrawingMode(true)
                viewModel.setUiState(EditingUiState.DRAW)
            }
            btnBack.setOnClickListener {
                setOnClickBtnBack()
            }
            btnRestore.setOnClickListener {
                ConfirmDialog.show(this@EditorActivity) {
                    imageEditorView.clearAllLayers()
                    val image = BitmapCacheManager.getBitmapFromMemCache(PREVIOUS_PHOTO_KEY)
                    imageEditorView.setImageBitmap(image!!)
                }
            }
            btnSaveCircle.setOnClickListener {

                when (viewModel.uiState.value) {
                    EditingUiState.STICKER -> {
                        imageEditorView.saveSticker()
                    }

                    EditingUiState.TEXT -> {
                        imageEditorView.saveText()
                    }

                    EditingUiState.FRAME -> {
                        imageEditorView.saveFrame()
                    }

                    EditingUiState.Filter -> {
                        imageEditorView.saveFilter()
                    }

                    EditingUiState.ADJUSTMENTS -> imageEditorView.saveAdjustments()

                    else -> {}
                }
                viewModel.setUiState(EditingUiState.MAIN)

            }
            btnSave.setOnClickListener {
                val bitmap = imageEditorView.getBitmap()
                val uri = viewModel.saveImageToGallery(bitmap)
                if (uri == null) {
                    return@setOnClickListener
                } else {
                    startActivity(Intent(this@EditorActivity, SaveActivity::class.java).apply {
                        putExtra("Uri", uri.toString())
                    })
                }
            }
            btnSticker.setOnClickListener { viewModel.setUiState(EditingUiState.STICKER) }
            btnCrop.setOnClickListener { viewModel.setUiState(EditingUiState.CROP) }
            btnFrame.setOnClickListener { viewModel.setUiState(EditingUiState.FRAME) }
            btnFilter.setOnClickListener { viewModel.setUiState(EditingUiState.Filter) }
            btnAdjustments.setOnClickListener {
                viewModel.setUiState(EditingUiState.ADJUSTMENTS)
            }
        }
        setupDrawControls()
        setupTextControls()
        setupCropControls()
        setupAdjustmentControls()
    }

    private fun setupDrawControls() {
        binding.apply {
            btnUndo.setOnClickListener { imageEditorView.undo() }
            btnRedo.setOnClickListener { imageEditorView.redo() }
            btnColorDraw.setOnClickListener {
                val currColor = imageEditorView.getPenColor()
                colorAdapter.setCurrentColor(currColor)

                if (layoutSize.isVisible) {
                    layoutSize.visibility = View.GONE
                    btnPaintSize.setBackgroundResource(R.drawable.bg_no_border)
                }
                if (rcvColor.isGone) {
                    rcvColor.fadeIn()
                    rcvColor.startAnimation(slideUpFadeIn)
                    btnColorDraw.setBackgroundResource(R.drawable.bg_white_border)
                } else {
                    rcvColor.visibility = View.GONE
                    rcvColor.startAnimation(slideUpFadeOut)
                    btnColorDraw.setBackgroundResource(R.drawable.bg_no_border)
                }

            }
            btnEraser.setOnClickListener { viewModel.setEraser(!viewModel.isEraser.value!!) }
            btnPaintSize.setOnClickListener {
                if (btnColorDraw.isVisible) {
                    rcvColor.visibility = View.GONE
                    btnColorDraw.setBackgroundResource(R.drawable.bg_no_border)
                }
                if (layoutSize.isGone) {
                    layoutSize.fadeIn()
                    btnPaintSize.setBackgroundResource(R.drawable.bg_white_border)
                } else {
                    layoutSize.visibility = View.GONE
                    btnPaintSize.setBackgroundResource(R.drawable.bg_no_border)
                }
            }
        }
    }

    private fun setupTextControls() {
        binding.apply {
            btnFont.setOnClickListener {
                val viewToFocus = if (rcvFont.isVisible) null else btnFont
                setFocusBtnText(viewToFocus)
            }
            btnColorText.setOnClickListener {
                val viewToFocus =
                    if (rcvColor.isVisible && layoutStrokeSize.isGone) null else btnColorText
                setFocusBtnText(viewToFocus)

            }
            btnStroke.setOnClickListener {
                val viewToFocus = if (layoutStrokeSize.isVisible) null else btnStroke
                setFocusBtnText(viewToFocus)
            }
            btnTextSize.setOnClickListener {
                val viewToFocus = if (layoutSizeText.isVisible) null else btnTextSize
                setFocusBtnText(viewToFocus)
            }
        }
    }

    private fun setFocusBtnDraw(view: View? = null) {
        binding.layoutBtnArtLinear.children.forEach {
            if (it == view) it.setBackgroundResource(R.drawable.bg_white_border)
            else {
                if (it == binding.btnEraser) binding.imageEditorView.setEraserMode(false)
                it.setBackgroundResource(R.drawable.bg_no_border)
            }
        }
    }

    private fun setFocusBtnText(view: View? = null) {
        binding.apply {
            btnFont.setBackgroundResource(R.drawable.bg_no_border)
            btnColorText.setBackgroundResource(R.drawable.bg_no_border)
            btnStroke.setBackgroundResource(R.drawable.bg_no_border)
            btnTextSize.setBackgroundResource(R.drawable.bg_no_border)
            if (rcvFont.isVisible) {
                rcvFont.visibility = View.GONE
                rcvFont.startAnimation(slideUpFadeOut)
            }
            if (rcvColor.isVisible) {
                rcvColor.visibility = View.GONE
                rcvColor.startAnimation(slideUpFadeOut)
            }

            if (layoutStrokeSize.isVisible) {
                layoutStrokeSize.visibility = View.GONE
                layoutStrokeSize.startAnimation(slideUpFadeOut)
            }
            if (layoutSizeText.isVisible) {
                layoutSizeText.visibility = View.GONE
                layoutSizeText.startAnimation(slideUpFadeOut)
            }
            when (view) {
                btnFont -> {
                    btnFont.setBackgroundResource(R.drawable.bg_white_border)
                    rcvFont.visibility = View.VISIBLE
                    rcvFont.startAnimation(slideUpFadeIn)
                }

                btnColorText -> {
                    btnColorText.setBackgroundResource(R.drawable.bg_white_border)
                    rcvColor.visibility = View.VISIBLE
                    rcvColor.startAnimation(slideUpFadeIn)
                    val currColor = imageEditorView.getTextColor()
                    colorAdapter.setCurrentColor(currColor)
                }

                btnStroke -> {
                    btnStroke.setBackgroundResource(R.drawable.bg_white_border)
                    rcvColor.visibility = View.VISIBLE
                    layoutStrokeSize.visibility = View.VISIBLE
                    val currColor = imageEditorView.getStrokeColor()
                    colorAdapter.setCurrentColor(currColor)
                    rcvColor.startAnimation(slideUpFadeIn)
                    seekbarSizeStroke.progress = imageEditorView.getStrokeWidth()!!
                    layoutStrokeSize.startAnimation(slideUpFadeIn)

                }

                btnTextSize -> {
                    btnTextSize.setBackgroundResource(R.drawable.bg_white_border)
                    layoutSizeText.visibility = View.VISIBLE
                    seekbarTextSize.progress = imageEditorView.getCurrentTextSizeInSp()?.toInt()!!
                    layoutSizeText.startAnimation(slideUpFadeIn)
                }
            }
        }
    }

    private fun onCropState() {
        binding.apply {
            imageEditorView.onClickItem()
            val originalBitmap = imageEditorView.getBitmap()
            val newWidth = (originalBitmap.width * SCALE_FACTOR).toInt()
            val newHeight = (originalBitmap.height * SCALE_FACTOR).toInt()

            val scaledBitmapForDisplay = originalBitmap.scale(newWidth, newHeight, true)
            cropImageView.resetCropRect()
            cropImageView.setAspectRatio(originalBitmap.width, originalBitmap.height)
            cropImageView.setFixedAspectRatio(false)
            cropImageView.scaleType = com.canhub.cropper.CropImageView.ScaleType.CENTER

            setFocusBtnRatio(btnCropFree)
            setOtherButtonsVisibility(root, listOf(), View.GONE)
            cropContainer.visibility = View.VISIBLE
            btnRestore.visibility = View.GONE
            cropImageView.setImageBitmap(scaledBitmapForDisplay)
        }
    }

    private fun endCropMode(shouldSaveChanges: Boolean) {
        if (shouldSaveChanges) {
            val originalBitmap = binding.imageEditorView.getBitmap()
            val cropRectOnScaledBitmap = binding.cropImageView.cropRect

            if (cropRectOnScaledBitmap != null) {
                val inverseScaleFactor = 1f / SCALE_FACTOR

                val originalCropX = (cropRectOnScaledBitmap.left * inverseScaleFactor).toInt()
                val originalCropY = (cropRectOnScaledBitmap.top * inverseScaleFactor).toInt()
                var originalCropWidth =
                    (cropRectOnScaledBitmap.width() * inverseScaleFactor).toInt()
                var originalCropHeight =
                    (cropRectOnScaledBitmap.height() * inverseScaleFactor).toInt()

                if (originalCropX + originalCropWidth > originalBitmap.width) {
                    originalCropWidth = originalBitmap.width - originalCropX
                }
                if (originalCropY + originalCropHeight > originalBitmap.height) {
                    originalCropHeight = originalBitmap.height - originalCropY
                }

                val croppedHighQualityBitmap = Bitmap.createBitmap(
                    originalBitmap,
                    originalCropX,
                    originalCropY,
                    originalCropWidth,
                    originalCropHeight
                )
                val currentPhoto = BitmapCacheManager.getBitmapFromMemCache(CURRENT_PHOTO_KEY)
                val bitmap = Bitmap.createBitmap(
                    currentPhoto!!,
                    originalCropX,
                    originalCropY,
                    originalCropWidth,
                    originalCropHeight
                )
                binding.imageEditorView.setImageBitmap(bitmap)
                val bitmapDraw = binding.imageEditorView.getBitmap()
                BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
                BitmapCacheManager.addBitmapToMemoryCache(CURRENT_PHOTO_KEY, bitmapDraw)
                binding.imageEditorView.clearAllLayers()
                binding.imageEditorView.setImageBitmap(croppedHighQualityBitmap)

            } else {
                Toast.makeText(this, "Lỗi khi cắt ảnh", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cropContainer.visibility = View.GONE
        viewModel.setUiState(EditingUiState.MAIN)
    }

    private fun setupCropControls() {
        binding.apply {
            btnBackCrop.setOnClickListener {
                endCropMode(false)
            }
            btnSaveCrop.setOnClickListener {
                endCropMode(true)
            }
            btnCrop11.setOnClickListener {
                val width = binding.imageEditorView.getImageWidth().toInt()
                cropImageView.setAspectRatio(width, width)
                cropImageView.setFixedAspectRatio(true)
                setFocusBtnRatio(btnCrop11)
            }
            btnCrop34.setOnClickListener {
                val width = binding.imageEditorView.getImageWidth().toInt()
                cropImageView.setAspectRatio(width, width * 4 / 3)
                cropImageView.setFixedAspectRatio(true)
                setFocusBtnRatio(btnCrop34)
            }
            btnCrop916.setOnClickListener {
                val width = binding.imageEditorView.getImageWidth().toInt()
                cropImageView.setAspectRatio(width, width * 16 / 9)
                cropImageView.setFixedAspectRatio(true)
                setFocusBtnRatio(btnCrop916)
            }
            btnCropLock.setOnClickListener {
                cropImageView.setFixedAspectRatio(true)
                setFocusBtnRatio(btnCropLock)
            }
            btnCropFree.setOnClickListener {
                cropImageView.setFixedAspectRatio(false)
                setFocusBtnRatio(btnCropFree)
            }
        }
    }

    private fun setFocusBtnRatio(view: View) {
        for (i in binding.layoutBtnCrop.children) {
            if (i == view) i.setBackgroundResource(R.drawable.bg_white_border)
            else i.setBackgroundResource(R.drawable.bg_no_border)
        }
    }

    private fun setOnClickBtnBack() {
        ConfirmDialog.show(this) {
            when (viewModel.uiState.value) {
                EditingUiState.MAIN -> {
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))
                }

                EditingUiState.DRAW -> {
                    if (viewModel.uiState.value == EditingUiState.DRAW) {
                        binding.imageEditorView.apply {
                            setDrawingMode(false)
                            clearCanvas()
                        }
                    }
                    viewModel.setUiState(EditingUiState.MAIN)
                }

                EditingUiState.STICKER -> {
                    binding.imageEditorView.exitSticker()
                    viewModel.setUiState(EditingUiState.MAIN)
                }

                EditingUiState.CROP -> {
                    BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
                    BitmapCacheManager.addBitmapToMemoryCache(
                        CURRENT_PHOTO_KEY, binding.imageEditorView.getBitmap()
                    )
                    viewModel.setUiState(EditingUiState.MAIN)
                }

                EditingUiState.FRAME -> {
                    binding.imageEditorView.exitFrame()
                    viewModel.setUiState(EditingUiState.MAIN)
                }

                EditingUiState.Filter -> {
                    viewModel.viewModelScope.launch {
                        viewModel.runWithLoading {
                            binding.imageEditorView.exitFilter()
                        }
                        viewModel.setUiState(EditingUiState.MAIN)
                    }
                }

                EditingUiState.ADJUSTMENTS -> {
                    binding.imageEditorView.exitAdjustments()
                    viewModel.setUiState(EditingUiState.MAIN)
                }

                else -> {
                }
            }
        }

    }

    private fun setUpBackPressCallBack() {
        val backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setOnClickBtnBack()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun onMainState() {
        binding.apply {
            setOtherButtonsVisibility(
                root, listOf(btnBack, btnSave, imageEditorView, layoutBtn, btnRestore), View.GONE
            )
            setOtherButtonsVisibility(
                layoutBtn, listOf(layoutBtnMain), View.GONE
            )
            setFocusBtnText()
            setFocusBtnDraw()
            imageEditorView.setDrawingMode(false)
            imageEditorView.setFrameMode(false)
            imageEditorView.setFilterMode(false)
            imageEditorView.setAdjustmentMode(false)
            cropImageView.setFixedAspectRatio(false)

        }
    }

    private fun onDrawState() {
        setOtherButtonsVisibility(
            binding.layoutBtn, listOf(), View.GONE
        )
        binding.apply {
            layoutBtnArt.fadeIn()
            btnRedo.fadeIn()
            btnUndo.fadeIn()
            btnRestore.visibility = View.GONE
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
        }

    }

    private fun onTextState() {
        if (binding.layoutBtnText.isGone) {
            setOtherButtonsVisibility(
                binding.layoutBtn, listOf(binding.layoutBtnText), View.GONE
            )
            binding.apply {
                btnBack.visibility = View.GONE
                layoutBtnText.fadeIn()
                btnSaveCircle.fadeIn()
                btnRestore.visibility = View.GONE
                btnSave.visibility = View.GONE
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
            } else childView.visibility = if (visibility == View.GONE) View.VISIBLE else View.GONE
        }
    }

    fun View.fadeIn(duration: Long = 300) {
        this.alpha = 0f
        this.visibility = View.VISIBLE
        this.animate().alpha(1f).setDuration(duration).start()
    }

    override fun onDestroy() {
        super.onDestroy()
        BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
    }
}