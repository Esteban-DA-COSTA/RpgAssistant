package com.estebandacosta.jdrassistant

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.estebandacosta.jdrassistant.home.HomeScreen
import com.estebandacosta.jdrassistant.server.GameServer
import com.estebandacosta.jdrassistant.theme.FantasyTheme

fun main() {
    val server = GameServer(port = 8080)
    server.start()
    application {
        Window(
            onCloseRequest = {
                server.stop()
                exitApplication()
            },
            title = "Assistant JdR",
            state = rememberWindowState(width = 1280.dp, height = 860.dp),
        ) {
            FantasyTheme {
                HomeScreen(onCreateWorld = {}, onOpenWorld = {})
            }
        }
    }
}
