package com.example.phocraft.ui.detail_photo

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityDetailPhotoBinding

class DetailPhotoActivity : AppCompatActivity() {
    private val binding by lazy { ActivityDetailPhotoBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setUpUi()
    }

    private fun setUpUi() {
        val bitmap = intent.getParcelableExtra<Bitmap>("bitmap")
//        Glide.with(this).load(bitmap).into(binding.iv)
    }
}