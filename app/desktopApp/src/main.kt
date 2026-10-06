package com.estebandacosta.jdrassistant

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Jdr Assistant") {
        App()
    }
}
