package io.scanbot.example

import io.scanbot.sdk.imagefilters.ParametricFilter

interface FiltersListener {

    fun onFilterApplied(parametricFilter: ParametricFilter?)
}
