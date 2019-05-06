package io.scanbot.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.scanbot.example.FiltersListener
import io.scanbot.example.R

/**
 * Represents bottom menu sheet for document screen
 */
class FiltersBottomSheetMenuFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.filters_bottom_sheet, container, false)

        view.findViewById<Button>(R.id.lowLightBinarizationFilter).setOnClickListener {
            (activity as FiltersListener).lowLightBinarizationFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.edgeHighlightFilter).setOnClickListener {
            (activity as FiltersListener).edgeHighlightFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.deepBinarizationFilter).setOnClickListener {
            (activity as FiltersListener).deepBinarizationFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.otsuBinarizationFilter).setOnClickListener {
            (activity as FiltersListener).otsuBinarizationFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.cleanBackgroundFilter).setOnClickListener {
            (activity as FiltersListener).cleanBackgroundFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.colorDocumentFilter).setOnClickListener {
            (activity as FiltersListener).colorDocumentFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.colorFilter).setOnClickListener {
            (activity as FiltersListener).colorFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.grayscaleFilter).setOnClickListener {
            (activity as FiltersListener).grayscaleFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.binarizedFilter).setOnClickListener {
            (activity as FiltersListener).binarizedFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.pureBinarizedFilter).setOnClickListener {
            (activity as FiltersListener).pureBinarizedFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.blackAndWhiteFilter).setOnClickListener {
            (activity as FiltersListener).blackAndWhiteFilter()
            dismissAllowingStateLoss()
        }
        view.findViewById<Button>(R.id.none).setOnClickListener {
            (activity as FiltersListener).noneFilter()
            dismissAllowingStateLoss()
        }
        return view
    }
}
