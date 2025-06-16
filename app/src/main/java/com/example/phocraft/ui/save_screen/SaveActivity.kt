package com.example.phocraft.ui.save_screen

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivitySaveBinding
import com.example.phocraft.ui.home.HomeActivity
import com.example.phocraft.utils.BitmapCacheManager
import com.example.phocraft.utils.CURRENT_PHOTO_KEY
import com.example.phocraft.utils.PREVIOUS_PHOTO_KEY
import com.example.phocraft.utils.shareBitmap

class SaveActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySaveBinding.inflate(layoutInflater) }
    private lateinit var uri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpUi()
        setOnclick()
    }

    private fun setUpUi() {
        uri = intent.getStringExtra("Uri")?.toUri() ?: "".toUri()
        Glide.with(this).load(uri).into(binding.iv)
        Glide.with(this).load(uri).into(binding.ivSmall)
    }

    private fun setOnclick() {
        binding.apply {
            btnHome.setOnClickListener {
                BitmapCacheManager.removeBitmapFromMemoryCache(CURRENT_PHOTO_KEY)
                BitmapCacheManager.removeBitmapFromMemoryCache(PREVIOUS_PHOTO_KEY)
                startActivity(Intent(this@SaveActivity, HomeActivity::class.java))
            }
        }
        binding.btnContinue.setOnClickListener {
            finish()
        }
        binding.btnShare.setOnClickListener {
            shareBitmap(this, uri)
        }
    }
}