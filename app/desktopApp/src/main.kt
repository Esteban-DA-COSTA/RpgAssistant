package com.estebandacosta.jdrassistant

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.estebandacosta.jdrassistant.server.GameServer

fun main() {
    val server = GameServer(port = 8080)
    server.start()
    application {
        Window(
            onCloseRequest = {
                server.stop()
                exitApplication()
            },
            title = "Jdr Assistant",
        ) {
            App()
        }
    }
}
