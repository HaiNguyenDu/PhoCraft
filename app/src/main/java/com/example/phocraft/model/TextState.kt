package com.example.phocraft.model

import android.graphics.Matrix
import android.graphics.Typeface

data class TextState(
    val text: String,
    val textColor: Int,
    val textSize: Float,
    val typeface: Typeface,
    val transformMatrix: Matrix
)