package com.example.phocraft.views

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.phocraft.R
import com.example.phocraft.databinding.DialogEditTextBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object DialogEditText {
    fun show(
        context: Context,
        currentText: String,
        customTextView: CustomTextView,
    ) {
        val dialog = BottomSheetDialog(context, R.style.AppBottomSheetDialogTheme)
        dialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            val binding = DialogEditTextBinding.inflate(LayoutInflater.from(context))
            setContentView(binding.root)

            setOnShowListener {
                val bottomSheet = findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
                if (bottomSheet != null) {
                    val layoutParams = bottomSheet.layoutParams
                    layoutParams.height = Resources.getSystem().displayMetrics.heightPixels
                    bottomSheet.layoutParams = layoutParams

                    val behavior = BottomSheetBehavior.from(bottomSheet)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    behavior.skipCollapsed = true
                }
            }

            binding.apply {
                edt.setText(currentText)
                btnSave.setOnClickListener {
                    val text = edt.text.toString()
                    customTextView.setText(text)
                    dismiss()
                }

                btnExit.setOnClickListener {
                    dismiss()
                }
            }
            show()
        }
    }
}
