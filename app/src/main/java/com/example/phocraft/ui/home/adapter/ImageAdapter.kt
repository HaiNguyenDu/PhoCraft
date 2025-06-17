package com.example.phocraft.ui.home.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.phocraft.databinding.ItemImageBinding
import com.example.phocraft.model.Image

class ImageAdapter(
    private val context: Context,
    private var listImage: List<Image>,
    private val onClickItem: (Uri) -> Unit,
) : RecyclerView.Adapter<ImageAdapter.ImageAdapterViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ImageAdapterViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ImageAdapterViewHolder,
        position: Int,
    ) {
        val image = listImage[position]
        holder.onHolder(image.uri)
    }

    fun submitList(newList: List<Image>) {
        listImage = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listImage.size

    inner class ImageAdapterViewHolder(private val binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onHolder(uri: Uri) {
            Glide.with(context)
                .load(uri)
                .into(binding.iv)
            binding.root.setOnClickListener { onClickItem(uri) }
        }
    }
}