package com.example.scanbot.utils

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams

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