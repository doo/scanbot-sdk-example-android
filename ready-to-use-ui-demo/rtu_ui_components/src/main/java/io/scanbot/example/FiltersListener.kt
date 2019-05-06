package io.scanbot.example

interface FiltersListener {
    fun lowLightBinarizationFilter()
    fun edgeHighlightFilter()
    fun deepBinarizationFilter()
    fun otsuBinarizationFilter()
    fun cleanBackgroundFilter()
    fun colorDocumentFilter()
    fun colorFilter()
    fun grayscaleFilter()
    fun binarizedFilter()
    fun pureBinarizedFilter()
    fun blackAndWhiteFilter()
    fun noneFilter()
}
