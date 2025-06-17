package com.example.phocraft.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import com.example.phocraft.databinding.DialogConfirmExitBinding

object ConfirmDialog {
    fun show(
        context: Context,
        onClickConfirm: () -> Unit
    ) {
        Dialog(context).apply {

            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            window?.setDimAmount(0.6f)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val binding = DialogConfirmExitBinding.inflate(layoutInflater)
            setContentView(binding.root)

            with(binding) {

                btnConfirm.setOnClickListener {
                    onClickConfirm()
                    dismiss()
                }
                btnCancel.setOnClickListener {
                    dismiss()
                }
            }

            show()
        }
    }
}