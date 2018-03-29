package yuyu.itplacenet.helpers

import android.Manifest.permission.*
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import yuyu.itplacenet.R

import yuyu.itplacenet.utils.*

class PermissionHelper(private val activity: Activity) {

    // Камера
    fun mayUseCamera(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (ContextCompat.checkSelfPermission(activity.applicationContext, CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity.applicationContext, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ) {
            return true
        }
        ActivityCompat.requestPermissions(activity, arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), RC_CHECK_PERMISSION_CAMERA)

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale(activity, WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(activity.findViewById(R.id.profile_photo), R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { ActivityCompat.requestPermissions(activity, arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), RC_CHECK_PERMISSION_CAMERA) })
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(CAMERA,WRITE_EXTERNAL_STORAGE), RC_CHECK_PERMISSION_CAMERA)
        }
        return false
    }

    // Определение метоположения
    fun mayGetDeviceLocation(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        if (ContextCompat.checkSelfPermission(activity.applicationContext, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            return true
        }
        ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), RC_CHECK_PERMISSION_LOCATION)

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
            Snackbar.make(activity.findViewById(R.id.map), R.string.permission_my_location, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                            { ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), RC_CHECK_PERMISSION_LOCATION) })
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), RC_CHECK_PERMISSION_LOCATION)
        }
        return false
    }
}