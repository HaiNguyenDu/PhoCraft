package com.example.phocraft.ui.editor.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.phocraft.R
import com.example.phocraft.databinding.ItemColorBinding

class ColorAdapter(
    private val context: Context,
    private var listColorRes: List<Int>,
    private val onClickItem: (color: Int?) -> Unit,
) : RecyclerView.Adapter<ColorAdapter.ColorAdapterViewHolder>() {
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorAdapterViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorAdapterViewHolder, position: Int) {
        val isNoneItem = (position == 0)
        holder.onHolder(isNoneItem, position == selectedPosition)
    }

    override fun getItemCount(): Int = listColorRes.size + 1
    fun setCurrentColor(currentColorValue: Int?) {
        val oldPosition = selectedPosition
        var newPosition = -1
        if (currentColorValue == ContextCompat.getColor(context, R.color.none)) {
            newPosition = 0
        } else {
            val foundIndex = listColorRes.indexOfFirst { resId ->
                ContextCompat.getColor(context, resId) == currentColorValue
            }
            if (foundIndex != -1) {
                newPosition = foundIndex + 1
            }
        }

        if (newPosition != selectedPosition) {
            selectedPosition = newPosition
            if (oldPosition != -1) notifyItemChanged(oldPosition)
            if (selectedPosition != -1) notifyItemChanged(selectedPosition)
        }
    }

    inner class ColorAdapterViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val clickedPosition = adapterPosition
                    if (selectedPosition == clickedPosition) return@setOnClickListener
                    val oldPosition = selectedPosition
                    selectedPosition = clickedPosition

                    if (oldPosition != -1) {
                        notifyItemChanged(oldPosition)
                    }
                    notifyItemChanged(selectedPosition)

                    if (selectedPosition == 0) {
                        onClickItem(ContextCompat.getColor(context, R.color.none))
                    } else {
                        val colorResId = listColorRes[selectedPosition - 1]
                        val colorValue = ContextCompat.getColor(context, colorResId)
                        onClickItem(colorValue)
                    }
                }
            }
        }

        fun onHolder(isNoneItem: Boolean, isSelected: Boolean) {
            binding.apply {
                if (isNoneItem) {
                    colorView.background =
                        ContextCompat.getDrawable(context, R.drawable.ic_color_none)
                    ViewCompat.setBackgroundTintList(colorView, null)
                } else {
                    colorView.background =
                        ContextCompat.getDrawable(context, R.drawable.bg_color_circle)
                    val colorResId = listColorRes[adapterPosition - 1]
                    val colorValue = ContextCompat.getColor(context, colorResId)
                    ViewCompat.setBackgroundTintList(colorView, ColorStateList.valueOf(colorValue))
                }
                colorItemContainer.isSelected = isSelected
            }
        }
    }
}
