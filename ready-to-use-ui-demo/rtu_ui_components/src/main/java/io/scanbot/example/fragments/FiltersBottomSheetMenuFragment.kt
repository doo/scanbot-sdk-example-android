package io.scanbot.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.scanbot.example.FiltersListener
import io.scanbot.example.R
import io.scanbot.imagefilters.BrightnessFilter
import io.scanbot.imagefilters.ColorDocumentFilter
import io.scanbot.imagefilters.ContrastFilter
import io.scanbot.imagefilters.CustomBinarizationFilter
import io.scanbot.imagefilters.GrayscaleFilter
import io.scanbot.imagefilters.LegacyFilter
import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.imagefilters.ScanbotBinarizationFilter
import io.scanbot.imagefilters.WhiteBlackPointFilter
import io.scanbot.sdk.process.ImageFilterType

/**
 * Represents bottom menu sheet for document screen
 */
class FiltersBottomSheetMenuFragment : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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

        view.findViewById<Button>(R.id.legacyLowLightBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.LOW_LIGHT_BINARIZATION.code))
        }
        view.findViewById<Button>(R.id.legacyLowLightBinarizationFilter2).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.LOW_LIGHT_BINARIZATION_2.code))
        }
        view.findViewById<Button>(R.id.legacyEdgeHighlightFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.EDGE_HIGHLIGHT.code))
        }
        view.findViewById<Button>(R.id.legacyDeepBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.DEEP_BINARIZATION.code))
        }
        view.findViewById<Button>(R.id.legacyOtsuBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.OTSU_BINARIZATION.code))
        }
        view.findViewById<Button>(R.id.legacyCleanBackgroundFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.BACKGROUND_CLEAN.code))
        }
        view.findViewById<Button>(R.id.colorDocumentFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.COLOR_DOCUMENT.code))
        }
        view.findViewById<Button>(R.id.legacyColorFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.COLOR_ENHANCED.code))
        }
        view.findViewById<Button>(R.id.grayscaleFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.GRAYSCALE.code))
        }
        view.findViewById<Button>(R.id.legacyBinarizedFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.BINARIZED.code))
        }
        view.findViewById<Button>(R.id.legacyPureBinarizedFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.PURE_BINARIZED.code))
        }
        view.findViewById<Button>(R.id.legacyBlackAndWhiteFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.BLACK_AND_WHITE.code))
        }
        view.findViewById<Button>(R.id.legacySensitiveBinarizationFilter).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.SENSITIVE_BINARIZATION.code)) // this relies on ImageProcessor.Type.ML_BASED - see comment in Application class
        }
        view.findViewById<Button>(R.id.none).setOnClickListener {
            callListenerAndDismiss(LegacyFilter(ImageFilterType.NONE.code))
        }
        return view
    }

    private fun callListenerAndDismiss(parametricFilter: ParametricFilter) {
        (activity as FiltersListener).onFilterApplied(parametricFilter)
        dismissAllowingStateLoss()
    }
}
