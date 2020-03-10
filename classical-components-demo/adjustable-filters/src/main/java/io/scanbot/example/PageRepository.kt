package io.scanbot.example

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.TuneOperation

class PageRepository {
    companion object {
        fun generatePreview(context: Context, page: Page, imageFilterType: ImageFilterType, tunes: List<TuneOperation>, filterOrder: Int) {
            ScanbotSDK(context).pageProcessor().generateFilteredPreview(page, imageFilterType, tunes, filterOrder)
        }

        fun applyFilter(context: Context, page: Page, imageFilterType: ImageFilterType, tunes: List<TuneOperation>, filterOrder: Int): Page {
            ScanbotSDK(context).pageProcessor().applyFilterTunes(page, imageFilterType, tunes, filterOrder)
            ScanbotSDK(context).pageProcessor().generateFilteredPreview(page, imageFilterType, tunes, filterOrder)
            return Page(pageId = page.pageId,
                    polygon = page.polygon,
                    detectionStatus = page.detectionStatus,
                    filter = imageFilterType,
                    tunes = tunes,
                    filterOrder = filterOrder)
        }
    }
}