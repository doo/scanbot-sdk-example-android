package io.scanbot.example

import io.scanbot.sdk.imagefilters.ParametricFilter

fun ParametricFilter?.getFilterName(): String {
    return this?.javaClass?.simpleName ?: "None"
}
