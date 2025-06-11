package com.example.phocraft.ui.editing.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phocraft.databinding.ItemStickerBinding

class StickerAdapter(
    private val listSticker: List<Bitmap>,
    private val onClickItem: (sticker: Bitmap) -> Unit,
) : RecyclerView.Adapter<StickerAdapter.StickerAdapterViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): StickerAdapterViewHolder {
        val binding =
            ItemStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StickerAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: StickerAdapterViewHolder,
        position: Int,
    ) {
        val sticker = listSticker[position]
        holder.onHolder(sticker)
    }

    override fun getItemCount(): Int = listSticker.size

    inner class StickerAdapterViewHolder(private val binding: ItemStickerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onHolder(sticker: Bitmap) {
            binding.apply {
                iv.setImageBitmap(sticker)
                root.setOnClickListener {
                    onClickItem(sticker)
                }
            }
        }
    }
}