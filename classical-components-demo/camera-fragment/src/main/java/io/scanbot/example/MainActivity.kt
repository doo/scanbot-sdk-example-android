package io.scanbot.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.show_dialog_btn).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), PERMISSIONS_REQUEST_CAMERA)
                }
            } else {
                openCameraDialog()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCameraDialog()
            }
        }
    }

    private fun openCameraDialog() {
        val ft = supportFragmentManager.beginTransaction()
        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // Create and show the dialog.
        val newFragment: DialogFragment = CameraDialogFragment.newInstance()
        newFragment.show(ft, "dialog")
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CAMERA = 314
    }
}