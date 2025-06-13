package com.example.phocraft.ui.editing

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityEditingBinding
import com.example.phocraft.enum.EditingUiState
import com.example.phocraft.ui.editing.adapter.ColorAdapter
import com.example.phocraft.ui.editing.adapter.FontAdapter
import com.example.phocraft.ui.editing.adapter.StickerAdapter
import com.example.phocraft.utils.BitmapCacheManager
import com.example.phocraft.utils.CONVERT_PAINT_WIDTH
import com.example.phocraft.utils.CURRENT_PHOTO_KEY

class EditingActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditingBinding.inflate(layoutInflater) }
    private val viewModel: EditingViewModel by viewModels()
    var slideUpFadeIn: Animation? = null
    var slideUpFadeOut: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setUpUi()
        setOnClick()
        observeLiveData()
        setUpSeekBar()
    }

    private fun setUpUi() {
        val currPhoto = BitmapCacheManager.getBitmapFromMemCache(CURRENT_PHOTO_KEY)
        if (currPhoto == null) {
            finish()
            return
        }
        binding.imageEditorView.setImageBitmap(currPhoto)
        binding.imageEditorView.onMainState = {
            onMainState()
        }
        slideUpFadeIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in)
        slideUpFadeOut = AnimationUtils.loadAnimation(this, R.anim.slide_down_fate_out)
        val listColorId = viewModel.listColor.value ?: emptyList()

        val colorAdapter = ColorAdapter(this, listColorId) { color ->
            if (viewModel.uiState.value == EditingUiState.DRAW) {
                binding.imageEditorView.setPenColor(color)
                viewModel.setEraser(false)
                Toast.makeText(this, R.string.color_change, Toast.LENGTH_SHORT).show()
            } else if (binding.layoutStrokeSize.isVisible) {
                binding.imageEditorView.setOutline(color)
            } else if (viewModel.uiState.value == EditingUiState.TEXT) {
                binding.imageEditorView.setTextColor(color)
                Toast.makeText(this, R.string.color_change, Toast.LENGTH_SHORT).show()
            }
        }

        binding.rcvColor.apply {
            layoutManager =
                LinearLayoutManager(this@EditingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }

        val listSticker = viewModel.stickers.value ?: emptyList()
        val stickerAdapter = StickerAdapter(listSticker) { sticker ->
            binding.imageEditorView.addSticker(sticker)
        }

        binding.rcvSticker.apply {
            layoutManager =
                LinearLayoutManager(this@EditingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = stickerAdapter
        }

        val listFont = viewModel.listFont.value ?: emptyList()
        val fontAdapter = FontAdapter(this, listFont) {
            binding.imageEditorView.setFont(it.typeface)
        }
        binding.rcvFont.apply {
            layoutManager =
                LinearLayoutManager(this@EditingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = fontAdapter
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
    }

    private fun observeLiveData() {
        viewModel.photo.observe(this) {
            binding.imageEditorView.setImageBitmap(it)
        }
        viewModel.uiState.observe(this) {
            when (it) {
                EditingUiState.MAIN -> {
                    onMainState()
                }

                EditingUiState.DRAW -> {
                    onDrawState()
                }

                EditingUiState.TEXT -> {
                    onTextState()
                }

                EditingUiState.STICKER -> {
                    onStickerState()
                }

                EditingUiState.CROP -> {
                    onCropState()
                }

                else -> {
                    onMainState()
                }
            }
        }
        viewModel.isEraser.observe(this) {
            binding.btnEraser.setBackgroundResource(if (it == true) R.drawable.bg_white_border else R.drawable.bg_no_border)
            binding.imageEditorView.setEraserMode(it)
        }
    }

    private fun onStickerState() {
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(),
            View.GONE
        )
        binding.apply {
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
            rcvSticker.fadeIn()
        }
    }

    private fun setOnClick() {
        binding.btnText.setOnClickListener {
            viewModel.setUiState(EditingUiState.TEXT)
            binding.imageEditorView.addText({
                onTextState()
            }, {
                viewModel.setUiState(EditingUiState.TEXT)
            })
        }
        binding.btnArt.setOnClickListener {
            binding.imageEditorView.setDrawingMode(true)
            viewModel.setUiState(EditingUiState.DRAW)
        }
        binding.btnBack.setOnClickListener {
            setOnClickBtnBack()
        }

        binding.btnSaveCircle.setOnClickListener {
            when (viewModel.uiState.value) {
                EditingUiState.STICKER -> {
                    binding.imageEditorView.saveSticker()
                }

                EditingUiState.TEXT -> {
                    binding.imageEditorView.saveText()
                }

                else -> {}
            }
            viewModel.setUiState(EditingUiState.MAIN)
        }

        binding.btnFilter.setOnClickListener {
            viewModel.setUiState(EditingUiState.STICKER)
        }
        binding.btnCrop.setOnClickListener {
            viewModel.setUiState(EditingUiState.CROP)
        }
        setOnClickDrawState()
        setOnClickTextState()
        setOnclickCropState()
    }

    private fun setOnClickDrawState() {
        binding.apply {
            btnUndo.setOnClickListener {
                imageEditorView.undo()
            }
            btnRedo.setOnClickListener {
                imageEditorView.redo()
            }
            btnColorDraw.setOnClickListener {
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
            binding.btnEraser.setOnClickListener {
                viewModel.setEraser(!viewModel.isEraser.value!!)
            }
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

    private fun setOnClickTextState() {
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
        }
    }

    private fun setFocusBtnDraw(view: View? = null) {
        binding.layoutBtnArtLinear.children.forEach {
            if (it == view)
                it.setBackgroundResource(R.drawable.bg_white_border)
            else {
                if (it == binding.btnEraser)
                    binding.imageEditorView.setEraserMode(false)
                it.setBackgroundResource(R.drawable.bg_no_border)
            }
        }
    }

    private fun setFocusBtnText(view: View? = null) {
        binding.apply {
            btnFont.setBackgroundResource(R.drawable.bg_no_border)
            btnColorText.setBackgroundResource(R.drawable.bg_no_border)
            btnStroke.setBackgroundResource(R.drawable.bg_no_border)

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
                }

                btnStroke -> {
                    btnStroke.setBackgroundResource(R.drawable.bg_white_border)
                    rcvColor.visibility = View.VISIBLE
                    layoutStrokeSize.visibility = View.VISIBLE
                    rcvColor.startAnimation(slideUpFadeIn)
                    seekbarSizeStroke.progress = imageEditorView.getStrokeWidth()!!
                    layoutStrokeSize.startAnimation(slideUpFadeIn)

                }
            }
        }
    }

    private fun onCropState() {
        binding.cropImageView.setAspectRatio(
            binding.imageEditorView.getBitmap().width,
            binding.imageEditorView.getBitmap().height
        )
        setFocusBtnRatio(binding.btnCropFree)
        val currentBitmap = binding.imageEditorView.getBitmap()
        if (currentBitmap == null) {
            Toast.makeText(this, "Không có ảnh để cắt", Toast.LENGTH_SHORT).show()
            return
        }
        setOtherButtonsVisibility(binding.root, listOf(), View.GONE)
        binding.cropContainer.visibility = View.VISIBLE
        binding.cropImageView.setImageBitmap(currentBitmap)
        binding.layoutBtnCrop.startAnimation(slideUpFadeIn)
    }

    private fun endCropMode(shouldSaveChanges: Boolean) {
        if (shouldSaveChanges) {

            val croppedBitmap = binding.cropImageView.getCroppedImage()
            binding.imageEditorView.clearAllLayers()
            binding.imageEditorView.setImageBitmap(
                croppedBitmap ?: binding.imageEditorView.getBitmap()
            )

        }
        onMainState()
    }

    private fun setOnclickCropState() {
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
            if (i == view)
                i.setBackgroundResource(R.drawable.bg_white_border)
            else
                i.setBackgroundResource(R.drawable.bg_no_border)
        }
    }

    private fun setOnClickBtnBack() {
        viewModel.setUiState(EditingUiState.MAIN)
        when (viewModel.uiState.value) {
            EditingUiState.MAIN -> {
                finish()
            }

            EditingUiState.DRAW -> {
                if (viewModel.uiState.value == EditingUiState.DRAW) {
                    binding.imageEditorView.apply {
                        setDrawingMode(false)
                        clearCanvas()
                    }
                }
            }

            EditingUiState.STICKER -> {
                binding.imageEditorView.exitSticker()
            }

            EditingUiState.TEXT -> {
                binding.imageEditorView.exitText()
            }

            EditingUiState.CROP -> {
                BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
                BitmapCacheManager.addBitmapToMemoryCache(
                    CURRENT_PHOTO_KEY,
                    binding.imageEditorView.getBitmap()
                )
            }

            else -> {
            }
        }
    }

    private fun onMainState() {
        binding.apply {
            setOtherButtonsVisibility(
                root,
                listOf(btnBack, btnSave, imageEditorView, layoutBtn),
                View.GONE
            )
            setOtherButtonsVisibility(
                layoutBtn,
                listOf(layoutBtnMain),
                View.GONE
            )
            setFocusBtnText()
            setFocusBtnDraw()
            imageEditorView.setDrawingMode(false)
        }
    }

    private fun onDrawState() {
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(),
            View.GONE
        )
        binding.apply {
            layoutBtnArt.fadeIn()
            btnRedo.fadeIn()
            btnUndo.fadeIn()
            btnSaveCircle.fadeIn()
            btnSave.visibility = View.GONE
        }

    }

    private fun onTextState() {
        if (binding.layoutBtnText.isGone) {
            setOtherButtonsVisibility(
                binding.layoutBtn,
                listOf(binding.layoutBtnText),
                View.GONE
            )
            binding.apply {
                btnBack.visibility = View.GONE
                layoutBtnText.fadeIn()
                btnSaveCircle.fadeIn()
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
            } else childView.visibility = if (visibility == View.GONE)
                View.VISIBLE else View.GONE
        }
    }

    fun View.fadeIn(duration: Long = 300) {
        this.alpha = 0f
        this.visibility = View.VISIBLE
        this.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
    }
}