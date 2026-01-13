package com.example.scanbot.preview

import io.scanbot.sdk.imageprocessing.ParametricFilter

interface FiltersListener {
    fun onFilterApplied(filter: ParametricFilter?)
}
