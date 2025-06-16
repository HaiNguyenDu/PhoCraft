package com.example.phocraft.ui.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.phocraft.databinding.ActivityOnboardingBinding
import com.example.phocraft.ui.home.HomeActivity
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {
    private val binding by lazy { ActivityOnboardingBinding.inflate(layoutInflater) }
    private lateinit var onboardingAdapter: OnboardingPageAdapter
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
        setupUi()

    }

    private fun setupUi() {
        onboardingAdapter = OnboardingPageAdapter(this)
        binding.viewPager.adapter = onboardingAdapter
        TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPager) { tab, position ->
        }.attach()
        binding.btnGetStart.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }


}