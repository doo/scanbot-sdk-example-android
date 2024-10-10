package com.example.scanbot.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.isVisible
import com.example.scanbot.sharing.SaveListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.scanbot.sdk.usecases.documents.R

/**
 * Represents bottom menu sheet for document screen with saving dialog
 */
class SaveBottomSheetMenuFragment(private val onePageMode: Boolean = false) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.save_bottom_sheet, container, false)

        view.findViewById<Button>(R.id.save_pdf).setOnClickListener {
            (activity as SaveListener).savePdf()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.save_tiff).setOnClickListener {
            (activity as SaveListener).saveTiff()
            dismissAllowingStateLoss()
        }

        val saveJpeg = view.findViewById<Button>(R.id.save_jpeg)
        saveJpeg.setOnClickListener {
            (activity as SaveListener).saveJpeg()
            dismissAllowingStateLoss()
        }

        val savePng = view.findViewById<Button>(R.id.save_png)
        savePng.setOnClickListener {
            (activity as SaveListener).savePng()
            dismissAllowingStateLoss()
        }
        savePng.isVisible = onePageMode
        saveJpeg.isVisible = onePageMode
        return view
    }
}
