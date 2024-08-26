package com.example.scanbot.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.scanbot.imagefilters.ColorDocumentFilter
import io.scanbot.imagefilters.GrayscaleFilter
import io.scanbot.imagefilters.LegacyFilter
import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.imagefilters.ScanbotBinarizationFilter
import io.scanbot.imagefilters.WhiteBlackPointFilter
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.usecases.documents.R

/** Represents bottom menu sheet for document screen. */
class FiltersBottomSheetMenuFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.filters_bottom_sheet, container, false)

        view.findViewById<Button>(R.id.colorDocumentFilter).setOnClickListener {
            callListenerAndDismiss(ColorDocumentFilter())
        }
        view.findViewById<Button>(R.id.grayscaleFilter).setOnClickListener {
            callListenerAndDismiss(GrayscaleFilter())
        }
        view.findViewById<Button>(R.id.binarizationFilter).setOnClickListener {
            callListenerAndDismiss(ScanbotBinarizationFilter())
        }
        view.findViewById<Button>(R.id.whiteBlackPointFilter).setOnClickListener {
            callListenerAndDismiss(WhiteBlackPointFilter())
        }
        view.findViewById<Button>(R.id.filterNone).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.NONE.code))
        }
        return view
    }

    private fun callListenerAndDismiss(filter: ParametricFilter) {
        (activity as FiltersListener).onFilterApplied(filter)
        dismissAllowingStateLoss()
    }
}
