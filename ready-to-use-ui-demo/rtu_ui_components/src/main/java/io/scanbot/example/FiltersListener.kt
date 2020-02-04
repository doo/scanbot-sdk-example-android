package io.scanbot.example

import io.scanbot.sdk.process.ImageFilterType

interface FiltersListener {
    fun onFilterApplied(filterType: ImageFilterType)
}
