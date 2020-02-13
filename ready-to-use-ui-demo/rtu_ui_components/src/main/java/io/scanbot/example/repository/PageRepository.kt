package io.scanbot.example.repository

import android.content.Context
import io.scanbot.sdk.ScanbotSDK
import io.scanbot.sdk.persistence.Page
import io.scanbot.sdk.process.ImageFilterType
import io.scanbot.sdk.process.TuneOperation

class PageRepository {

    companion object {

        private val pages = mutableListOf<Page>()

        fun getPages(): List<Page> = pages

        fun removePage(context: Context, pageToRemove: Page) {
            ScanbotSDK(context).pageFileStorage().remove(pageToRemove.pageId)
            pages.remove(pageToRemove)
        }

        fun addPages(newPages: List<Page>) {
            pages.addAll(newPages)
        }

        fun clearPages(context: Context) {
            ScanbotSDK(context).pageFileStorage().removeAll()

            pages.clear()
        }

        fun applyFilter(context: Context, imageFilterType: ImageFilterType) {
            pages.forEach {
                ScanbotSDK(context).pageProcessor().applyFilterTunes(it, imageFilterType, it.tunes, it.filterOrder)
            }
            val list = pages.map {
                Page(pageId = it.pageId,
                        polygon = it.polygon,
                        detectionStatus = it.detectionStatus,
                        filter = imageFilterType,
                        tunes = it.tunes,
                        filterOrder = it.filterOrder)
            }.toMutableList()

            pages.clear()
            pages.addAll(list)
        }

        fun generatePreview(context: Context, page: Page, imageFilterType: ImageFilterType, tunes: List<TuneOperation>, filterOrder: Int) {
            pages.first { it.pageId == page.pageId }.apply {
                ScanbotSDK(context).pageProcessor().generateFilteredPreview(this, imageFilterType, tunes, filterOrder)
            }
        }

        fun applyFilter(context: Context, page: Page, imageFilterType: ImageFilterType, tunes: List<TuneOperation>, filterOrder: Int): Page {
            pages.forEach {
                if (it.pageId == page.pageId) {
                    ScanbotSDK(context).pageProcessor().applyFilterTunes(it, imageFilterType, tunes, filterOrder)
                    ScanbotSDK(context).pageProcessor().generateFilteredPreview(it, imageFilterType, tunes, filterOrder)
                }
            }
            val result = Page(pageId = page.pageId,
                    polygon = page.polygon,
                    detectionStatus = page.detectionStatus,
                    filter = imageFilterType,
                    tunes = tunes,
                    filterOrder = filterOrder)
            val list = pages.map {
                if (it.pageId == page.pageId) {
                    result
                } else {
                    it
                }
            }.toMutableList()

            pages.clear()
            pages.addAll(list)
            return result
        }

        fun updatePage(page: Page): Page {
            val index = pages.indexOfFirst { it.pageId == page.pageId }
            pages.removeAll {
                it.pageId == page.pageId
            }
            pages.add(index, page)
            return page
        }

        fun addPage(page: Page) {
            pages.add(page)
        }
    }

}