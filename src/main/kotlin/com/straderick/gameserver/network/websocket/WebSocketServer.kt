package com.straderick.gameserver.network.websocket

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.websockets
import org.http4k.server.Netty
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.http4k.core.Method
import org.http4k.routing.ws.bind
import java.util.concurrent.ConcurrentHashMap



class WebSocketServer(
    private val port: Int,
    private val scope: CoroutineScope
) {
    private val server by lazy {
        val ws = createWebSocketApp()
        ws.asServer(Netty(port))
    }

    private val connections = ConcurrentHashMap.newKeySet<Websocket>()

    private fun createWebSocketApp(): HttpHandler {
        val wsHandler: (Websocket) -> Unit = { ws: Websocket ->
            println("Client connected!")
            connections.add(ws)

            ws.onMessage { message ->
                println("Received message: ${message.body.toString()}")
                // Broadcast to all clients
                broadcast(message.body.toString())
            }

            ws.onClose {
                connections.remove(ws)
                println("Client disconnected")
            }

            ws.onError { error ->
                println("Error occurred: ${error.message}")
            }
        }

        return routes(
            "/ws" bind websockets(wsHandler),
            "/" bind Method.GET to { _: Request -> Response(OK).body("WebSocket Server Running") }
        )
    }

    fun start() {
        server.start()
        println("WebSocket server started on port $port")
    }

    fun broadcast(message: String) {
        scope.launch {
            connections.forEach { socket ->
                socket.send(WsMessage(message))
            }
        }
    }

    fun stop() {
        server.stop()
    }
}
