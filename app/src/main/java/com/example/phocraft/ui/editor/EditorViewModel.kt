package com.example.phocraft.ui.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.phocraft.data.repositories.ColorRepository
import com.example.phocraft.data.repositories.FilterRepository
import com.example.phocraft.data.repositories.FontRepository
import com.example.phocraft.data.repositories.FrameRepository
import com.example.phocraft.data.repositories.ImageRepository
import com.example.phocraft.data.repositories.StickerRepository
import com.example.phocraft.enum.AdjustmentsState
import com.example.phocraft.enum.EditingUiState
import com.example.phocraft.model.FilterItem
import com.example.phocraft.model.FontItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorViewModel(application: Application) : AndroidViewModel(application) {
    private val imageRepository = ImageRepository(application)
    private val colorRepository = ColorRepository()
    private val stickerRepository = StickerRepository()
    private val fontRepository = FontRepository()
    private val frameRepository = FrameRepository()
    private val filterRepository = FilterRepository()

    private val _filters = MutableLiveData<MutableList<FilterItem>>()
    val filters: LiveData<MutableList<FilterItem>> get() = _filters

    private val _stickers =
        MutableLiveData<List<Bitmap>>()
    val stickers: LiveData<List<Bitmap>> get() = _stickers

    private val _photo = MutableLiveData<Bitmap>()
    val photo: LiveData<Bitmap> get() = _photo

    private val _uiState = MutableLiveData<EditingUiState>(EditingUiState.MAIN)
    val uiState: LiveData<EditingUiState> get() = _uiState

    private val _listColor = MutableLiveData<List<Int>>()
    val listColor: LiveData<List<Int>> get() = _listColor

    private val _isEraser = MutableLiveData<Boolean>(false)
    val isEraser: LiveData<Boolean> get() = _isEraser

    private val _listFont = MutableLiveData<List<FontItem>>()
    val listFont: LiveData<List<FontItem>> get() = _listFont

    private val _listFrame = MutableLiveData<List<Bitmap>>()
    val listFrame: LiveData<List<Bitmap>> get() = _listFrame

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _adjustmentsState = MutableLiveData<AdjustmentsState>()
    val adjustmentsState: LiveData<AdjustmentsState> = _adjustmentsState

    init {
        viewModelScope.launch {
            _stickers.value = stickerRepository.loadStickersFromAssets(getApplication())
            _listColor.value = colorRepository.getListColorId()
            _listFont.value = fontRepository.loadFontsFromAssets(getApplication())
            _listFrame.value = frameRepository.getBitmapListFromAssetFolder(getApplication())
        }
    }

    fun setPhoto(uri: Uri) {
        val bitmap = BitmapFactory.decodeStream(
            getApplication<Application>().contentResolver.openInputStream(uri)
        )
        _photo.value = bitmap
    }

    fun setUiState(state: EditingUiState) {
        _uiState.value = state
    }

    fun setAdjustmentsState(state: AdjustmentsState) {
        if (adjustmentsState.value == state)
            _adjustmentsState.value = AdjustmentsState.NONE
        else _adjustmentsState.value = state
    }

    fun setEraser(state: Boolean) {
        _isEraser.value = state
    }

    fun setUpListFilter(originalBitmap: Bitmap) {
        viewModelScope.launch {
            _filters.value = filterRepository.setUpFilterList(originalBitmap).toMutableList()
        }
    }

    suspend fun runWithLoading(block: suspend () -> Unit) {
        _isLoading.value = true
        try {
            withContext(Dispatchers.Default) {
                block()
            }
        } catch (e: Exception) {
            Log.e("ViewModel", "Lỗi trong tác vụ nền", e)
        } finally {
            _isLoading.value = false
        }
    }

    fun saveImageToGallery(bitmap: Bitmap): Uri? {
        return imageRepository.saveImage(bitmap)
    }
}