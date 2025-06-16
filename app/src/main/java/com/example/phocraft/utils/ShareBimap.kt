package com.example.phocraft.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun shareBitmap(context: Context, uri: Uri, chooserTitle: String = "share photos with ...") {

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/png"
    }

    context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
}