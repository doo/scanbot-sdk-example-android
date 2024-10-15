package com.example.scanbot.preview

import io.scanbot.sdk.imagefilters.ParametricFilter

interface FiltersListener {
    fun onFilterApplied(filter: ParametricFilter)
}
