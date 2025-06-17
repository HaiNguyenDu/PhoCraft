package com.example.phocraft.data.repositories

import android.graphics.Bitmap
import androidx.core.graphics.scale
import com.example.phocraft.enum.FilterType
import com.example.phocraft.model.FilterItem
import com.example.phocraft.utils.FilterManager

class FilterRepository() {
    fun setUpFilterList(originalBitmap: Bitmap): List<FilterItem> {
        val scaleBitmap = originalBitmap.scale((0.2*originalBitmap.width).toInt(),(0.2*originalBitmap.height).toInt())
        val filterItems = listOf(
            FilterItem("None", scaleBitmap, FilterType.NONE),
            FilterItem(
                "Sepia",
                FilterManager.applyFilter(scaleBitmap, FilterType.SEPIA),
                FilterType.SEPIA
            ),
            FilterItem(
                "Noise",
                FilterManager.applyFilter(scaleBitmap, FilterType.NOISE),
                FilterType.NOISE
            ),
            FilterItem(
                "Invert",
                FilterManager.applyFilter(scaleBitmap, FilterType.INVERT),
                FilterType.INVERT
            )
        )
        return filterItems
    }
}