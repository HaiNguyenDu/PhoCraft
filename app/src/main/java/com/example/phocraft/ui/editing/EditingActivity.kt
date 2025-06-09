package com.example.phocraft.ui.editing

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityEditingBinding
import java.text.DecimalFormat

class EditingActivity : AppCompatActivity() {
    private val binding by lazy { ActivityEditingBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
    }
}