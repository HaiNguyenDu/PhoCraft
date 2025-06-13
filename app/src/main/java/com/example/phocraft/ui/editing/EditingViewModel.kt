package com.example.phocraft.ui.editing

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.phocraft.data.repositories.ColorRepository
import com.example.phocraft.data.repositories.FontRepository
import com.example.phocraft.data.repositories.StickerRepository
import com.example.phocraft.enum.EditingUiState
import com.example.phocraft.model.FontItem

class EditingViewModel(application: Application) : AndroidViewModel(application) {
    private val colorRepository = ColorRepository()
    private val stickerRepository = StickerRepository()
    private val fontRepository = FontRepository()
    private val _stickers =
        MutableLiveData<List<Bitmap>>(stickerRepository.loadStickersFromAssets(getApplication()))
    val stickers: LiveData<List<Bitmap>> get() = _stickers

    private val _photo = MutableLiveData<Bitmap>()
    val photo: LiveData<Bitmap> get() = _photo

    private val _uiState = MutableLiveData<EditingUiState>(EditingUiState.MAIN)
    val uiState: LiveData<EditingUiState> get() = _uiState

    private val _listColor = MutableLiveData<List<Int>>(colorRepository.getListColorId())
    val listColor: LiveData<List<Int>> get() = _listColor

    private val _isEraser = MutableLiveData<Boolean>(false)
    val isEraser: LiveData<Boolean> get() = _isEraser

    private val _listFont =
        MutableLiveData<List<FontItem>>(fontRepository.loadFontsFromAssets(getApplication()))
    val listFont: LiveData<List<FontItem>> get() = _listFont

    fun setPhoto(uri: Uri) {
        val bitmap = BitmapFactory.decodeStream(
            getApplication<Application>().contentResolver.openInputStream(uri)
        )
        _photo.value = bitmap
    }

    fun setUiState(state: EditingUiState) {
        _uiState.value = state
    }

    fun setEraser(state: Boolean) {
        _isEraser.value = state
    }
}