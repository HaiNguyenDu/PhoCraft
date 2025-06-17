package com.example.phocraft.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.phocraft.R
import com.example.phocraft.databinding.ActivityHomeBinding
import com.example.phocraft.ui.camera.CameraActivity
import com.example.phocraft.ui.home.adapter.PageAdapter
import com.example.phocraft.utils.AppPermissionUtils.isAndroid_Tiramisu_AndAbove
import com.example.phocraft.utils.AppPermissionUtils.isHasPermission
import com.example.phocraft.utils.AppPermissionUtils.showDialogGoToSetting
import com.example.phocraft.utils.AppPermissionUtils.showPermissionRationale
import com.example.phocraft.utils.FAVOURITE_FRAGMENT
import com.example.phocraft.utils.RECENT_FRAGMENT
import com.example.phocraft.utils.SELFIES_FRAGMENT
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {
    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private val permissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    binding.tvPs.visibility = View.GONE
                    binding.btnRequest.visibility = View.GONE
                    setUpUi()
                }

                shouldShowRequestPermissionRationale(IMAGE_PERMISSION) -> {
                    showPermissionRationale(
                        this,
                        getString(R.string.message_dialog_request_permission),
                        getString(R.string.necessary_permission)
                    ) {
                        requestPermissionImage()
                    }
                }

                else -> {
                    showDialogGoToSetting(
                        this, packageName, getString(R.string.lb_can_not_work_properly),
                        getString(R.string.necessary_permission)
                    )
                }
            }
        }
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in CAMERA_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                showPermissionRationale(
                    this,
                    getString(R.string.message_dialog_request_permission),
                    getString(R.string.necessary_permission)
                ) {
                    requestPermissionImage()
                }
            } else {
                checkPermissionCamera()

            }
        }

    override fun onResume() {
        super.onResume()
        if (isHasPermission(this, IMAGE_PERMISSION)) {
            binding.apply {
                btnRequest.visibility = View.GONE
                tvPs.visibility = View.GONE
            }
            setUpUi()
        }
    }

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
        checkReadPermissionImage()
        setUpBtn()
    }


    private fun setUpUi() {
        val tabTitles = arrayOf(RECENT_FRAGMENT, FAVOURITE_FRAGMENT, SELFIES_FRAGMENT)

        binding.apply {
            page.adapter = PageAdapter(this@HomeActivity)
            TabLayoutMediator(tabLayout, page) { tab, position ->
                val customTab =
                    LayoutInflater.from(tab.parent?.context).inflate(R.layout.custom_tab, null)
                val tv = customTab.findViewById<TextView>(R.id.tv_custom_tab)
                tv.text = tabTitles[position]
                tab.customView = customTab
            }.attach()
        }
    }

    private fun setUpBtn() {
        binding.btnCamera.setOnClickListener {
            checkPermissionCamera()
        }
        binding.btnRequest.setOnClickListener {
            requestPermissionImage()
        }
    }

    private fun checkReadPermissionImage() {
        if (isAndroid_Tiramisu_AndAbove()) {
            IMAGE_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES
        }

        if (isHasPermission(this, IMAGE_PERMISSION)) {
            setUpUi()
        } else {
            requestPermissionImage()
        }
    }

    private fun checkPermissionCamera() {
        if (
            CAMERA_PERMISSIONS.all {
                isHasPermission(this, it)
            }
        )
            startActivity(Intent(this, CameraActivity::class.java))
        else requestPermissionCamera()
    }

    private fun requestPermissionCamera() {
        activityResultLauncher.launch(CAMERA_PERMISSIONS)
    }

    private fun requestPermissionImage() {
        permissionLauncher.launch(IMAGE_PERMISSION)
    }

    companion object {
        private var IMAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
        private val CAMERA_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}