package io.scanbot.example

import io.scanbot.imagefilters.ParametricFilter

interface FiltersListener {
    fun onFilterApplied(parametricFilter: ParametricFilter)
}
