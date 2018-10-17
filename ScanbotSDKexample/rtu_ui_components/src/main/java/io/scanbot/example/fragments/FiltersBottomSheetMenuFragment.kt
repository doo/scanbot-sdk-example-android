package io.scanbot.example.fragments

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import io.scanbot.example.R

/**
 * Represents bottom menu sheet for document screen
 */
class FiltersBottomSheetMenuFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.filters_bottom_sheet, container, false)

        view.findViewById<Button>(R.id.cleanBackgroundFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.colorDocumentFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.colorFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.grayscaleFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.binarizedFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.pureBinarizedFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.blackAndWhiteFilter).setOnClickListener {
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.none).setOnClickListener {
            dismissAllowingStateLoss()
        }
        return view
    }
}
