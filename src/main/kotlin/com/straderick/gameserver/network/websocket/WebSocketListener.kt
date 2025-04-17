package com.straderick.gameserver.network.websocket

import com.straderick.gameserver.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

// TODO research coroutine scopes more and understand if there is a better way to handle them
class WebSocketListener(
    private val scope: CoroutineScope
) : WebSocketListener() {

    // using shared flow for broadcasting messages to multiple consumers
    private val _messageFlow = MutableSharedFlow<WebSocketMessage>()
    val messageFlow = _messageFlow.asSharedFlow()

    private val _connectionState = Channel<ConnectionState>()
    val connectionState = _connectionState.receiveAsFlow()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Logger.info("onOpen")
        scope.launch {
            _connectionState.send(ConnectionState.Connected)
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        scope.launch {
            _messageFlow.emit(WebSocketMessage.Text(text))
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        scope.launch {
            _messageFlow.emit(WebSocketMessage.Binary(bytes))
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        scope.launch {
            _connectionState.send(ConnectionState.Closing(code, reason))
        }
        webSocket.close(code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        scope.launch {
            _connectionState.send(ConnectionState.Closed(code, reason))
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Logger.info("onFailure")
        scope.launch {
            _connectionState.send(ConnectionState.Failed(t))
        }
    }
}

class SocketAbortedException : Exception()

sealed class WebSocketMessage {
    data class Text(val content: String) : WebSocketMessage()
    data class Binary(val content: ByteString) : WebSocketMessage()
}

sealed class ConnectionState {
    object Connected : ConnectionState()
    data class Closing(val code:Int, val reason: String) : ConnectionState()
    data class Closed(val code:Int, val reason: String) : ConnectionState()
    data class Failed(val error: Throwable) : ConnectionState()
}