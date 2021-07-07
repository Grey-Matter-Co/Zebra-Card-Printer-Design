package mx.com.infotecno.zebracardprinter.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.os.BuildCompat
import androidx.fragment.app.Fragment

object ExecutingDevicesHelper {
	fun hasSdkHigherThan(sdk: Int): Boolean = //Early previous of R will return Build.VERSION.SDK_INT as 29
		if (Build.VERSION_CODES.R == sdk) BuildCompat.isAtLeastR()
		else Build.VERSION.SDK_INT > sdk

	fun requestStoragePermission(fragment: Fragment, requestCode: Int) =
		fragment.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCode)

	fun requestCameraPermission(fragment: Fragment, requestCode: Int) =
		fragment.requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)

	fun hasStoragePermission(context: Context): Boolean =
		hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)

	fun hasCameraPermission(context: Context): Boolean =
		hasPermission(context, Manifest.permission.CAMERA)

	private fun hasPermission(context: Context, permission: String): Boolean =
		ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}