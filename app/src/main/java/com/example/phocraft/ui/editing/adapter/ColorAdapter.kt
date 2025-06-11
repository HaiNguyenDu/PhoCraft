package com.example.phocraft.ui.editing.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.phocraft.databinding.ItemColorBinding

class ColorAdapter(
    private val context: Context,
    private val listColor: List<Int>,
    private val onClickItem: (color: Int) -> Unit,
) : RecyclerView.Adapter<ColorAdapter.ColorAdapterViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ColorAdapterViewHolder {
        val binding = ItemColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ColorAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ColorAdapterViewHolder,
        position: Int,
    ) {
        val color = context.getColor(listColor[position])
        holder.onHolder(color)
    }

    override fun getItemCount(): Int = listColor.size

    inner class ColorAdapterViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onHolder(color: Int) {
            binding.apply {
                ViewCompat.setBackgroundTintList(
                    root,
                    ColorStateList.valueOf(color)
                )
                root.setOnClickListener {
                    onClickItem(color)
                }
            }
        }
    }
}
