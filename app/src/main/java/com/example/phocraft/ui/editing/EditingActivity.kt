package com.example.phocraft.ui.editing

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityEditingBinding
import com.example.phocraft.enum.EditingUiState
import com.example.phocraft.ui.editing.adapter.ColorAdapter
import com.example.phocraft.ui.editing.adapter.StickerAdapter
import com.example.phocraft.utils.CONVERT_PAINT_WIDTH
import com.example.phocraft.utils.GlobalValue

class EditingActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditingBinding.inflate(layoutInflater) }
    private val viewModel: EditingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setUpUi()
        setOnClick()
        observeLiveData()
    }

    private fun setUpUi() {

        if (GlobalValue.currPhotoUri == null) return
        viewModel.setPhoto(GlobalValue.currPhotoUri!!)

        val listColorId = viewModel.listColor.value ?: emptyList()

        val colorAdapter = ColorAdapter(this, listColorId) { color ->
            binding.imageEditorView.setPenColor(color)
            viewModel.setEraser(false)
            Toast.makeText(this, R.string.color_change, Toast.LENGTH_SHORT).show()
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
            viewModel.setUiState(state = EditingUiState.TEXT)
        }
        binding.btnArt.setOnClickListener {
            binding.imageEditorView.setDrawingMode(true)
            viewModel.setUiState(EditingUiState.DRAW)
        }
        binding.btnBack.setOnClickListener {
            setOnClickBtnBack()
        }

        binding.btnSaveCircle.setOnClickListener {
            if(viewModel.uiState.value == EditingUiState.STICKER){
                binding.imageEditorView.saveSticker()
            }
            viewModel.setUiState(EditingUiState.MAIN)
        }

        binding.btnFilter.setOnClickListener {
            viewModel.setUiState(EditingUiState.STICKER)
        }
        setOnClickDrawState()
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
                    btnColorDraw.setBackgroundResource(R.drawable.bg_white_border)
                } else {
                    rcvColor.visibility = View.GONE
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

    private fun setOnClickBtnBack() {
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
                viewModel.setUiState(EditingUiState.MAIN)
            }

            EditingUiState.STICKER -> {
                if (viewModel.uiState.value == EditingUiState.DRAW) {
                    binding.imageEditorView.apply {
                        setDrawingMode(false)
                        clearCanvas()
                    }
                }
                viewModel.setUiState(EditingUiState.MAIN)
                binding.imageEditorView.exitSticker()
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
        setOtherButtonsVisibility(
            binding.layoutBtn,
            listOf(binding.layoutBtnText),
            View.GONE
        )
        binding.layoutBtnText.fadeIn()
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
}