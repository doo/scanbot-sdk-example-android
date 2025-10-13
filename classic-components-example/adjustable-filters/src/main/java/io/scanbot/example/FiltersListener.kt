package io.scanbot.example

import io.scanbot.sdk.imageprocessing.ParametricFilter

interface FiltersListener {

    fun onFilterApplied(parametricFilter: ParametricFilter?)
}
