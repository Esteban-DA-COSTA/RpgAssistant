package com.estebandacosta.jdrassistant

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
