package com.example.phocraft.ui.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.phocraft.data.repositories.ImageRepository
import com.example.phocraft.enum.CameraSize
import com.example.phocraft.enum.FilterMode
import com.example.phocraft.enum.FlashState
import com.example.phocraft.enum.TimerState
import com.example.phocraft.model.CameraUiState
import com.example.phocraft.utils.DrawFilterHelper
import com.example.phocraft.utils.cropImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CameraViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableLiveData(CameraUiState())
    val uiState: LiveData<CameraUiState> = _uiState

    private var countdownJob: Job? = null

    private val _latestFaces = MutableLiveData<List<Face>>()
    val latestFaces: LiveData<List<Face>> = _latestFaces

    fun updateLatestFaces(faces: List<Face>) {
        _latestFaces.value = faces
    }

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
        _uiState.value?.let {
            _uiState.value = it.copy(gridState = !it.gridState)
        }
    }

    fun onFilterSelected(mode: FilterMode, bitmap: Bitmap?) {
        _uiState.value = _uiState.value?.copy(filterMode = mode, filterBitmap = bitmap)
    }

    fun onTakePhoto(captureAction: () -> Unit) {
        val currentState = _uiState.value ?: return
        if (currentState.isTakingPicture) {
            countdownJob?.cancel()
            _uiState.value = currentState.copy(isTakingPicture = false, countdownValue = null)
            return
        }

        val timerSeconds = when (currentState.timerState) {
            TimerState.T_3 -> 3
            TimerState.T_5 -> 5
            TimerState.T_10 -> 10
            TimerState.OFF -> 0
        }

        countdownJob = viewModelScope.launch {
            _uiState.value = currentState.copy(isTakingPicture = true)
            if (timerSeconds > 0) {
                for (i in timerSeconds downTo 1) {
                    _uiState.value = _uiState.value?.copy(countdownValue = i)
                    delay(1000)
                }
            }
            _uiState.value = _uiState.value?.copy(countdownValue = null)
            captureAction()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    suspend fun drawThenCropBitmap(
        imageProxy: ImageProxy,
        window: Window,
        cameraSize: CameraSize,
    ): Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        imageProxy.close()
        val matrix = Matrix().apply {
            if (_uiState.value?.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                postScale(-1f, 1f)
            }
        }

        val originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val correctedBitmap = Bitmap.createBitmap(
            originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
        )


        val uiState = _uiState.value ?: return correctedBitmap
        val filterBitmap = uiState.filterBitmap
        val filterMode = uiState.filterMode
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)
        val inputImage = InputImage.fromBitmap(correctedBitmap, 0)
        val faces = detector.process(inputImage).await()

        val bitmapToCrop =
            if (filterBitmap != null && filterMode != FilterMode.NONE && faces.isNotEmpty()) {
                val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                if (face == null) {
                    correctedBitmap
                } else {
                    val boundingBox = RectF(face.boundingBox)
                    val resultBitmapWithFilter = correctedBitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val canvas = Canvas(resultBitmapWithFilter)
                    val isFront = uiState.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                    val matrix = Matrix()
                    if (isFront) {
                        matrix.postScale(
                            -1f,
                            1f,
                            correctedBitmap.width / 2f,
                            correctedBitmap.height / 2f
                        )
                    }

                    matrix.mapRect(boundingBox)

                    if (filterMode == FilterMode.HEAD) {
                        DrawFilterHelper.drawHeadFilter(
                            face,
                            canvas,
                            boundingBox,
                            matrix,
                            isFront,
                            filterBitmap
                        )
                    } else if (filterMode == FilterMode.CHEEK) {
                        DrawFilterHelper.drawCheekFilter(
                            face,
                            canvas,
                            boundingBox,
                            matrix,
                            isFront,
                            filterBitmap
                        )
                    }
                    resultBitmapWithFilter
                }
            } else {
                correctedBitmap
            }

        val crop = cropImage(window, bitmapToCrop, _uiState.value?.cameraSize!!)

        return crop!!
    }


    fun updateSeekBarValue(value: Int?) {
        _uiState.value = _uiState.value?.copy(brightness = value)
    }

    fun onCaptureFinished() {
        _uiState.value = _uiState.value?.copy(isTakingPicture = false)
    }
}
