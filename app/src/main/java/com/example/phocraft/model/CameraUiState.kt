package com.example.phocraft.model

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import com.example.phocraft.enum.CameraSize
import com.example.phocraft.enum.FilterMode
import com.example.phocraft.enum.FlashState
import com.example.phocraft.enum.TimerState

data class CameraUiState(
    val flashState: FlashState = FlashState.OFF,
    val timerState: TimerState = TimerState.OFF,
    val cameraSize: CameraSize = CameraSize.S4_3,
    val gridState: Boolean = true,
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    val filterMode: FilterMode = FilterMode.HEAD,
    val filterBitmap: Bitmap? = null,
    val countdownValue: Int? = null,
    val isTakingPicture: Boolean = false,
    val showMainControls: Boolean = false,
    val brightness: Int? = null,
)