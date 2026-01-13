package io.scanbot.example.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.scanbot.example.FiltersListener
import io.scanbot.example.R
import io.scanbot.sdk.imageprocessing.BrightnessFilter
import io.scanbot.sdk.imageprocessing.ColorDocumentFilter
import io.scanbot.sdk.imageprocessing.ContrastFilter
import io.scanbot.sdk.imageprocessing.CustomBinarizationFilter
import io.scanbot.sdk.imageprocessing.GrayscaleFilter
import io.scanbot.sdk.imageprocessing.ParametricFilter
import io.scanbot.sdk.imageprocessing.ScanbotBinarizationFilter
import io.scanbot.sdk.imageprocessing.WhiteBlackPointFilter

/** Represents bottom menu sheet for page filters screen. */
class FiltersBottomSheetMenuFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.filters_bottom_sheet, container, false)

        view.findViewById<Button>(R.id.colorDocumentFilter).setOnClickListener {
            callListenerAndDismiss(ColorDocumentFilter())
        }
        view.findViewById<Button>(R.id.scanbotBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(ScanbotBinarizationFilter())
        }
        view.findViewById<Button>(R.id.customBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(CustomBinarizationFilter())
        }
        view.findViewById<Button>(R.id.brightnessFilter).setOnClickListener {
            callListenerAndDismiss(BrightnessFilter())
        }
        view.findViewById<Button>(R.id.contrastFilter).setOnClickListener {
            callListenerAndDismiss(ContrastFilter())
        }
        view.findViewById<Button>(R.id.grayscaleFilter).setOnClickListener {
            callListenerAndDismiss(GrayscaleFilter())
        }
        view.findViewById<Button>(R.id.whiteBlackPointFilter).setOnClickListener {
            callListenerAndDismiss(WhiteBlackPointFilter())
        }

        view.findViewById<Button>(R.id.none).setOnClickListener {
            callListenerAndDismiss(null)
        }
        return view
    }

    private fun callListenerAndDismiss(parametricFilter: ParametricFilter?) {
        (activity as FiltersListener).onFilterApplied(parametricFilter)
        dismissAllowingStateLoss()
    }
}
