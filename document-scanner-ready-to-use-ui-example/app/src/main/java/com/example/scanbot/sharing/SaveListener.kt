package com.example.scanbot.sharing

interface SaveListener {
    fun savePdf()
    fun saveTiff()
    fun saveJpeg() {}
    fun savePng() {}
}
