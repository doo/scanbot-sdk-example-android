package com.example.scanbot.preview

import io.scanbot.imagefilters.ParametricFilter

interface FiltersListener {
    fun onFilterApplied(filter: ParametricFilter)
}
