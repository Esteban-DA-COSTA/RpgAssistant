package com.estebandacosta.jdrassistant.server

import com.estebandacosta.jdrassistant.sayHello
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

/** Serveur de partie embarqué par l'app du MJ, à l'écoute du réseau local sur [port]. */
class GameServer(port: Int) {
    private val engine = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)

    /** Démarre l'écoute sans bloquer l'appelant. */
    fun start() {
        engine.start(wait = false)
    }

    /** Arrête le serveur et libère le port. */
    fun stop() {
        engine.stop(gracePeriodMillis = 500, timeoutMillis = 2_000)
    }
}

internal fun Application.module() {
    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }
    }
}
