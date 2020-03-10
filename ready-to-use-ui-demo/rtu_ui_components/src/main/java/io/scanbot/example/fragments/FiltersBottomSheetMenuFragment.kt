package io.scanbot.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.scanbot.example.FiltersListener
import io.scanbot.example.R
import io.scanbot.sdk.process.ImageFilterType

/**
 * Represents bottom menu sheet for document screen
 */
class FiltersBottomSheetMenuFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.filters_bottom_sheet, container, false)

        view.findViewById<Button>(R.id.lowLightBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.LOW_LIGHT_BINARIZATION)
        }
        view.findViewById<Button>(R.id.lowLightBinarizationFilter2).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.LOW_LIGHT_BINARIZATION_2)
        }
        view.findViewById<Button>(R.id.edgeHighlightFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.EDGE_HIGHLIGHT)
        }
        view.findViewById<Button>(R.id.deepBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.DEEP_BINARIZATION)
        }
        view.findViewById<Button>(R.id.otsuBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.OTSU_BINARIZATION)
        }
        view.findViewById<Button>(R.id.cleanBackgroundFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.BACKGROUND_CLEAN)
        }
        view.findViewById<Button>(R.id.colorDocumentFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.COLOR_DOCUMENT)
        }
        view.findViewById<Button>(R.id.colorFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.COLOR_ENHANCED)
        }
        view.findViewById<Button>(R.id.grayscaleFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.GRAYSCALE)
        }
        view.findViewById<Button>(R.id.binarizedFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.BINARIZED)
        }
        view.findViewById<Button>(R.id.pureBinarizedFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.PURE_BINARIZED)
        }
        view.findViewById<Button>(R.id.blackAndWhiteFilter).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.BLACK_AND_WHITE)
        }
        view.findViewById<Button>(R.id.none).setOnClickListener {
            callListenerAndDismiss(ImageFilterType.NONE)
        }
        return view
    }

    private fun callListenerAndDismiss(imageFilterType: ImageFilterType) {
        (activity as FiltersListener).onFilterApplied(imageFilterType)
        dismissAllowingStateLoss()
    }
}
