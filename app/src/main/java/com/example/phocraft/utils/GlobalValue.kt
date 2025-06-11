package com.example.phocraft.utils

import android.net.Uri

object GlobalValue {
    var currPhotoUri: Uri? = null

    fun resetCurrPhoto() {
        if (currPhotoUri != null)
            currPhotoUri = null
    }

    fun setValueCurrPhoto(uri: Uri) {
        currPhotoUri = uri
    }
}