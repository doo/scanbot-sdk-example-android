package io.scanbot.example.repository

import android.content.Context
import io.scanbot.example.di.ExampleSingletonImpl
import io.scanbot.sdk.imagefilters.ParametricFilter
import io.scanbot.sdk.persistence.page.legacy.Page

class PageRepository {
    companion object {
        private val pages = mutableListOf<Page>()

        fun getPages(): List<Page> = pages

        fun removePage(context: Context, pageToRemove: Page) {
            ExampleSingletonImpl(context).pageFileStorageInstance().remove(pageToRemove.pageId)
            pages.remove(pageToRemove)
        }

        fun addPages(newPages: List<Page>) {
            pages.addAll(newPages)
        }

        fun clearPages(context: Context) {
            ExampleSingletonImpl(context).pageFileStorageInstance().removeAll()

            pages.clear()
        }

        fun applyFilter(context: Context, parametricFilter: ParametricFilter) {
            pages.forEach {
                ExampleSingletonImpl(context).pageProcessorInstance().applyFilter(it, parametricFilter)
            }
            val list = pages.map {
                Page(pageId = it.pageId,
                        polygon = it.polygon,
                        detectionStatus = it.detectionStatus,
                        parametricFilter = parametricFilter,)
            }.toMutableList()

            pages.clear()
            pages.addAll(list)
        }

        fun generatePreview(context: Context, page: Page, parametricFilter: ParametricFilter) {
            pages.first { it.pageId == page.pageId }.apply {
                ExampleSingletonImpl(context).pageProcessorInstance().generateFilteredPreview(this, parametricFilter)
            }
        }

        fun applyFilter(context: Context, page: Page, parametricFilter: ParametricFilter): Page {
            val processor = ExampleSingletonImpl(context).pageProcessorInstance()
            pages.forEach {
                if (it.pageId == page.pageId) {
                    processor.applyFilter(it, parametricFilter)
                    processor.generateFilteredPreview(it, parametricFilter)
                }
            }
            val result = Page(pageId = page.pageId,
                    polygon = page.polygon,
                    detectionStatus = page.detectionStatus,
                    parametricFilter = parametricFilter,)
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