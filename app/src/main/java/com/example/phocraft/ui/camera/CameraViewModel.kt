package com.example.phocraft.ui.camera

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.phocraft.data.repositories.ImageRepository
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ImageRepository(application)
    fun saveImageToGallery(bitmap: Bitmap) {
        viewModelScope.launch {
            val result = repository.saveImage(bitmap)


        }
    }
}