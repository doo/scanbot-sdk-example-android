package io.scanbot.example

import io.scanbot.imagefilters.ParametricFilter
import io.scanbot.sdk.docprocessing.PageProcessor
import io.scanbot.sdk.persistence.Page

object PageFilterHelper {
    fun applyFilter(
        pageProcessor: PageProcessor,
        page: Page,
        parametricFilter: ParametricFilter,
    ): Page {
        pageProcessor.applyFilter(page, parametricFilter)
        pageProcessor.generateFilteredPreview(page, parametricFilter)
        return Page(
            pageId = page.pageId,
            polygon = page.polygon,
            detectionStatus = page.detectionStatus,
            parametricFilter = parametricFilter,
        )
    }
}