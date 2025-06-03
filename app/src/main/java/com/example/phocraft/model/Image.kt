package com.example.phocraft.model

import android.net.Uri

data class Image(
    val uri: Uri,
    val dateAdded: Long,
    val isFavorite: Boolean = false,
    val isSelfie: Boolean = false,
)