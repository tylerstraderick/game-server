package com.straderick.gameserver

import com.straderick.gameserver.config.ServerConfig
import com.straderick.gameserver.network.websocket.WebSocketServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class Application {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val config = ServerConfig()
    private lateinit var webSocketServer: WebSocketServer
    private val shutdownLatch = CountDownLatch(1)

    fun start() {
        webSocketServer = WebSocketServer(
            port = config.port,
            scope = scope
        )

        scope.launch {
            webSocketServer.start()
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            shutdown()
            shutdownLatch.countDown()
        })

        try {
            shutdownLatch.await()
        } catch (e: InterruptedException) {
            shutdown()
        }
    }

    private fun shutdown() {
        webSocketServer.stop()
        scope.coroutineContext.cancelChildren()
    }
}