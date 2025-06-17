package com.example.phocraft.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.phocraft.R
import com.example.phocraft.views.PermissionRationaleDialog

object AppPermissionUtils {

    fun isAndroid_Tiramisu_AndAbove() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun showDialogGoToSetting(
        context: Context,
        packageName: String,
        message: String,
        title: String,
    ) {
        PermissionRationaleDialog.with(context)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveLabel(context.getString(R.string.to_setting))
            .setPositiveButtonClick {
                toSettings(packageName, context)
            }
            .show()
    }

    fun showPermissionRationale(
        context: Context,
        message: String,
        title: String,
        callback: () -> Unit,
    ) {
        PermissionRationaleDialog.with(context)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButtonClick {
                callback.invoke()
            }
            .show()
    }

    private fun toSettings(packageName: String, context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun isHasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context, permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
