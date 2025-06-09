package com.example.phocraft.ui.camera

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phocraft.R
import com.example.phocraft.enum.CameraSize
import com.example.phocraft.enum.FilterMode
import com.example.phocraft.enum.FlashState
import com.example.phocraft.enum.TimerState
import com.example.phocraft.model.CameraUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CameraViewModel : ViewModel() {

    private val _uiState = MutableLiveData(CameraUiState())
    val uiState: LiveData<CameraUiState> = _uiState

    private val _filterBitmap = MutableLiveData<Bitmap?>()
    val filterBitmap: LiveData<Bitmap?> = _filterBitmap

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    fun onFlashToggled() {
        val currentState = _uiState.value ?: return
        val nextState = when (currentState.flashState) {
            FlashState.OFF -> FlashState.AUTO
            FlashState.AUTO -> FlashState.ON
            FlashState.ON -> FlashState.OFF
        }
        _uiState.value = currentState.copy(flashState = nextState)
    }

    fun onCameraSwapped() {
        val currentState = _uiState.value ?: return
        val nextSelector = if (currentState.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        _uiState.value = currentState.copy(cameraSelector = nextSelector)
    }

    fun onTimerSelected(timer: TimerState) {
        _uiState.value = _uiState.value?.copy(timerState = timer)
    }

    fun onSizeSelected(size: CameraSize) {
        _uiState.value = _uiState.value?.copy(cameraSize = size)
    }

    fun onGridToggled() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(gridState = !currentState.gridState)
    }

    fun onMainControlsToggled() {
        val currentState = _uiState.value ?: return
        _uiState.value = currentState.copy(showMainControls = !currentState.showMainControls)
    }

    fun onFilterSelected(mode: FilterMode, context: Context) {
        val bitmap = when (mode) {
            FilterMode.HEAD -> BitmapFactory.decodeResource(
                context.resources,
                R.drawable.filter_rabbit
            )

            FilterMode.CHEEK -> BitmapFactory.decodeResource(
                context.resources,
                R.drawable.filter_rabbit_cheek
            )

            FilterMode.NONE -> null
        }
        _filterBitmap.value = bitmap
        _uiState.value = _uiState.value?.copy(filterMode = mode)
    }

    fun onTakePhoto(saveImageAction: () -> Unit) {
        val currentState = _uiState.value ?: return
        if (currentState.isTakingPicture) return // Đang chụp rồi, không làm gì cả

        val timerSeconds = when (currentState.timerState) {
            TimerState.T_3 -> 3
            TimerState.T_5 -> 5
            TimerState.T_10 -> 10
            TimerState.OFF -> 0
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isTakingPicture = true)
            if (timerSeconds > 0) {
                for (i in timerSeconds downTo 1) {
                    _uiState.value = _uiState.value?.copy(countdownValue = i)
                    delay(1000)
                }
            }
            _uiState.value = _uiState.value?.copy(countdownValue = null)
            saveImageAction() // Gọi hàm chụp ảnh từ Activity
        }
    }

    fun onCaptureFinished() {
        _uiState.value = _uiState.value?.copy(isTakingPicture = false)
    }

    fun saveImageToGallery(context: Context, bitmap: Bitmap) {
//        val resolver = context.contentResolver
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, "phocraft_${System.currentTimeMillis()}.jpg")
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PhoCraft")
//            }
//        }
//
//        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//        uri?.let {
//            try {
//                resolver.openOutputStream(it)?.use { outputStream ->
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//                    _toastMessage.value = "Image saved"
//                }
//            } catch (e: Exception) {
//                _toastMessage.value = "Failed to save image"
//                Log.e("CameraVM", "Error saving image", e)
//            }
//        }
    }

    fun onToastShown() {
        _toastMessage.value = null
    }
}
