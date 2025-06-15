package com.example.phocraft.ui.editor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phocraft.databinding.ItemFilterBinding
import com.example.phocraft.enum.FilterType
import com.example.phocraft.model.FilterItem

class FilterAdapter(
    private var listFilters: MutableList<FilterItem>,
    private val onClickItem: (filter: FilterItem) -> Unit,
) : RecyclerView.Adapter<FilterAdapter.FilterAdapterViewHolder>() {
    private var selectedPosition = 0
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FilterAdapterViewHolder {
        val binding =
            ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterAdapterViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FilterAdapterViewHolder,
        position: Int,
    ) {
        val sticker = listFilters[position]
        holder.onHolder(sticker, position == selectedPosition)
    }

    fun updateFilters(newList: List<FilterItem>) {
        listFilters = newList.toMutableList()
        selectedPosition = 0
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listFilters.size
    fun setCurrentFilter(filterType: FilterType?) {
        if (filterType == null) {
            notifyItemChanged(selectedPosition)
            notifyItemChanged(0)
        }
        val oldPosition = selectedPosition
        var newPosition = -1
        val foundIndex = listFilters.indexOfFirst { item ->
            item.type == filterType
        }
        if (foundIndex != -1) {
            newPosition = foundIndex
        }
        notifyItemChanged(oldPosition)
        notifyItemChanged(selectedPosition)
    }

    inner class FilterAdapterViewHolder(private val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition != selectedPosition) {
                    val oldPosition = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)
                    onClickItem(listFilters[selectedPosition])
                }
            }
        }

        fun onHolder(filterItem: FilterItem, isSelected: Boolean) {
            binding.apply {
                binding.apply {
                    iv.setImageBitmap(filterItem.thumbnail)
                    tv.text = filterItem.name

                    // Chỉ cần đặt trạng thái isSelected cho itemView.
                    // Selector drawable sẽ tự động thay đổi background.
                    itemView.isSelected = isSelected
                }
            }
        }
    }
}