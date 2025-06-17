package com.example.phocraft.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.phocraft.data.repositories.ImageRepository
import com.example.phocraft.enum.ImageCategory
import com.example.phocraft.model.Image
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ImageRepository(application)
    private val _recentImages = MutableLiveData<List<Image>>()
    val recentImages: LiveData<List<Image>> = _recentImages
    private val _favoriteImages = MutableLiveData<List<Image>>()
    val favoriteImages: LiveData<List<Image>> = _favoriteImages

    private val _selfieImages = MutableLiveData<List<Image>>()
    val selfieImages: LiveData<List<Image>> = _selfieImages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadImages(category: ImageCategory) {
        viewModelScope.launch {
            _isLoading.value = true
            val images = repository.getImagesFromMediaStore(category)

            when (category) {
                ImageCategory.RECENT -> _recentImages.value = images
                ImageCategory.FAVORITE -> _favoriteImages.value = images
                ImageCategory.SELFIES -> _selfieImages.value = images
            }

            _isLoading.value = false
        }
    }
}
