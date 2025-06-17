package com.example.phocraft.model

import android.graphics.Bitmap
import com.example.phocraft.enum.FilterType

data class FilterItem(
    val name: String,
    val thumbnail: Bitmap,
    val type: FilterType
)