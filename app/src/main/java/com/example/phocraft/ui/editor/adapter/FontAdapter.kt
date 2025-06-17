package com.example.phocraft.ui.editor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phocraft.databinding.ItemFontBinding
import com.example.phocraft.model.FontItem

class FontAdapter(
    private val context: Context,
    private val listFont: List<FontItem>,
    private val onClickItem: (font: FontItem) -> Unit,
) : RecyclerView.Adapter<FontAdapter.FontAdapterViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FontAdapterViewHolder {
        val binding = ItemFontBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FontAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FontAdapterViewHolder,
        position: Int,
    ) {
        var font = listFont[position]
        holder.onHolder(font)
    }

    override fun getItemCount(): Int = listFont.size

    inner class FontAdapterViewHolder(private val binding: ItemFontBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onHolder(font: FontItem) {
            binding.apply {
                tv.text = font.displayName
                tv.typeface = font.typeface
                tv.setOnClickListener {
                    onClickItem(font)
                }
            }
        }
    }
}