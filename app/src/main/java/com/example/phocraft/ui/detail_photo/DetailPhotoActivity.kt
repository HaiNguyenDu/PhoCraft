package com.example.phocraft.ui.detail_photo

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.phocraft.databinding.ActivityDetailPhotoBinding
import com.example.phocraft.ui.editor.EditorActivity
import com.example.phocraft.utils.BitmapCacheManager
import com.example.phocraft.utils.CURRENT_PHOTO_KEY
import com.example.phocraft.utils.PREVIOUS_PHOTO_KEY

class DetailPhotoActivity : AppCompatActivity() {
    private val binding by lazy { ActivityDetailPhotoBinding.inflate(layoutInflater) }
    lateinit var bitmap: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        setUpUi()
        setOnClick()
    }

    private fun setUpUi() {
        val bitmapCache = BitmapCacheManager.getBitmapFromMemCache(CURRENT_PHOTO_KEY)
        bitmap = bitmapCache ?: return finish()
        Glide.with(this)
            .load(bitmap)
            .into(binding.ivMain)
    }

    private fun setOnClick() {
        binding.btnEdit.setOnClickListener {
            BitmapCacheManager.addBitmapToMemoryCache(PREVIOUS_PHOTO_KEY, bitmap)
            finish()
            startActivity(Intent(this, EditorActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
            BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
        }
    }
}