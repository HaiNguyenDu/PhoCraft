package com.example.phocraft.ui.editing

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityEditingBinding
import com.example.phocraft.enum.EditingUiState
import com.example.phocraft.ui.editing.adapter.ColorAdapter
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
            binding.imageEditorView.drawView?.setPaintColor(color)
            Toast.makeText(this, R.string.color_change, Toast.LENGTH_SHORT).show()
        }

        binding.rcvColor.apply {
            layoutManager =
                LinearLayoutManager(this@EditingActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = colorAdapter
        }

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

                else -> {
                    onMainState()
                }
            }
        }
        viewModel.isEraser.observe(this) {
            binding.imageEditorView.drawView?.setEraser(it)
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
            when (viewModel.uiState.value) {
                EditingUiState.MAIN -> {
                    finish()
                }

                else -> {
                    if (viewModel.uiState.value == EditingUiState.DRAW) {
                        binding.imageEditorView.setDrawingMode(false)
                    }
                    viewModel.setUiState(EditingUiState.MAIN)
                    onMainState()
                }
            }
        }
        binding.btnEraser.setOnClickListener {
            viewModel.setEraser(!viewModel.isEraser.value!!)
        }

        setOnClickDrawState()
    }

    private fun setOnClickDrawState() {
        binding.apply {
            btnUndo.setOnClickListener {
                imageEditorView.drawView?.undo()
            }
            btnRedo.setOnClickListener {
                imageEditorView.drawView?.redo()
            }
            btnColorDraw.setOnClickListener {
                if (rcvColor.isGone)
                    rcvColor.fadeIn()
                else
                    rcvColor.visibility = View.GONE
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
                listOf(),
                View.GONE
            )
            layoutBtnMain.fadeIn()
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
            }
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