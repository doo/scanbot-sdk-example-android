package io.scanbot.example

import io.scanbot.sdk.imageprocessing.ParametricFilter


fun ParametricFilter?.getFilterName(): String {
    return this?.javaClass?.simpleName ?: "None"
}
