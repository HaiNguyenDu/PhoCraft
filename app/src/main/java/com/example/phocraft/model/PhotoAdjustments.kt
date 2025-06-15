package com.example.phocraft.model

import com.example.phocraft.enum.FilterType

data class PhotoAdjustments(
    var brightness: Int = 0,
    var contrast: Float = 1.0f,
    var saturation: Float = 1.0f,
    var hue: Float = 0.0f,
)
