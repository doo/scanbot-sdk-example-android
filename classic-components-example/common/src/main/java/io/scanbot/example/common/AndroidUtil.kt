package io.scanbot.example.common

import android.app.Activity
import android.content.Context
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import io.scanbot.example.common.Const.APP_DATA_FOLDER_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

fun getAppStorageDir(context: Context) = File(context.getExternalFilesDir(null)!!, APP_DATA_FOLDER_NAME)

private fun plainShowToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Context.showToast(message: String) {
    // Only attempt to show toast on UI thread
    if (Looper.myLooper() == Looper.getMainLooper()) {
        plainShowToast(this, message)
    } else {
        if (this@showToast is CoroutineScope) {
            this.launch(Dispatchers.Main) {
                plainShowToast(this@showToast, message)
            }
        } else {
            Log.e(Const.LOG_TAG, "Can't show toast from non-UI thread without CoroutineScope!!!")
        }
    }
}

fun Activity.applyEdgeToEdge(rootView: View) {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
        val insets = windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin = insets.top
            bottomMargin = insets.bottom
            leftMargin = insets.left
            rightMargin = insets.right
        }
        WindowInsetsCompat.CONSUMED
    }
}
