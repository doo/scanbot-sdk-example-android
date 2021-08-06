package io.scanbot.example

import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.TuneOperation

object PageFilterHelper {
    fun applyFilter(
        pageProcessor: PageProcessor,
        page: Page,
        imageFilterType: ImageFilterType,
        tunes: List<TuneOperation>,
        filterOrder: Int
    ): Page {
        pageProcessor.applyFilterTunes(page, imageFilterType, tunes, filterOrder)
        pageProcessor.generateFilteredPreview(page, imageFilterType, tunes, filterOrder)
        return Page(
            pageId = page.pageId,
            polygon = page.polygon,
            detectionStatus = page.detectionStatus,
            filter = imageFilterType,
            tunes = tunes,
            filterOrder = filterOrder
        )
    }
}