package com.example.scanbot.main

enum class UseCase {
    SINGLE_PAGE,
    MULTIPLE_PAGE,
    FINDER,
    GALLERY,
}


sealed class ViewType(val type: Int) {
    class Header(val title: String) : ViewType(0)
    class Option(val useCase: UseCase, val title: String) : ViewType(1)
    class Support : ViewType(2)
}