package com.straderick.gameserver.network.websocket

import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

class WebSocketProvider(
    private val port: Int,
    private val scope: CoroutineScope
) {
    private var webSocket: WebSocket? = null
    private val socketOkHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    fun startSocket() {
        val listener = WebSocketListener(scope)
        val request = Request.Builder()
            .url("ws://localhost:$port")
            .build()

        webSocket = socketOkHttpClient.newWebSocket(request, listener)
    }

    fun stopSocket() {
            webSocket?.close(1000, null)
            socketOkHttpClient.dispatcher.executorService.shutdown()
    }
}