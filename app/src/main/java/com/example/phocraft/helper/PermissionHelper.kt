// PermissionHelper.kt
package com.example.phocraft.helper

import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {
    fun request(
        activity: Activity,
        permission: String,
        onDontHavePermissionMessage: String,
        permissionLauncher: ActivityResultLauncher<String>,
        onGranted: () -> Unit,
    ) {
        val status = ContextCompat.checkSelfPermission(activity, permission)
        if (status == PackageManager.PERMISSION_GRANTED) {
            onGranted()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, onDontHavePermissionMessage, Toast.LENGTH_SHORT).show()
            }
            permissionLauncher.launch(permission)
        }
    }
}
