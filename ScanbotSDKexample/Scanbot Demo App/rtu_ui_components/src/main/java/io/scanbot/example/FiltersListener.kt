package io.scanbot.example

interface FiltersListener {
    fun cleanBackgroundFilter()
    fun colorDocumentFilter()
    fun colorFilter()
    fun grayscaleFilter()
    fun binarizedFilter()
    fun pureBinarizedFilter()
    fun blackAndWhiteFilter()
    fun noneFilter()

}
