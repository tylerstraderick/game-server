package com.straderick.gameserver.config

class ServerConfig {
    val port: Int = System.getenv("SERVER_PORT")?.toIntOrNull() ?: 8080
    val host: String = System.getenv("SERVER_HOST") ?: "localhost"
}