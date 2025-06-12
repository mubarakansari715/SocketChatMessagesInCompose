package com.example.socketchatmessagesincompose.data.repository

import com.example.socketchatmessagesincompose.ui.model.Chat
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import timber.log.Timber
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton


class SocketManagerImpl @Inject constructor(
    private val socket: Socket
) : SocketManager {
    private var connectionCallback: ((Boolean) -> Unit)? = null
    private var messageCallback: ((Chat) -> Unit)? = null

    init {
        setupListeners()
    }

    private fun setupListeners() {
        socket.on(Socket.EVENT_CONNECT) {
            Timber.d("Socket connected successfully")
            connectionCallback?.invoke(true)
        }

        socket.on(Socket.EVENT_DISCONNECT) {
            Timber.d("Socket disconnected")
            connectionCallback?.invoke(false)
        }

        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            Timber.e("Socket connection error: ${args.joinToString()}")
            connectionCallback?.invoke(false)
        }

        registerSocketEvents()
    }

    private fun registerSocketEvents() {
        socket?.on(CHAT_KEYS.BROADCAST) { args ->
            args?.let { d ->
                if (d.isNotEmpty()) {
                    try {
                        val data = d[0]
                        if (data.toString().isNotEmpty()) {
                            val chat = Gson().fromJson(data.toString(), Chat::class.java)
                            messageCallback?.invoke(chat)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing message")
                    }
                }
            }
        }
    }

    override fun connect(onConnectionChange: (Boolean) -> Unit) {
        // Store callback
        connectionCallback = onConnectionChange

        // Check socket state
        if (socket?.connected() == true) {
            Timber.d("Socket already connected")
            onConnectionChange(true)
            return
        }

        // Not connected, so notify connecting state
        Timber.d("Socket not connected, connecting now")
        onConnectionChange(false)

        // Connect without adding duplicate listeners
        // The listeners in setupSocket() will handle connection events
        socket?.connect()
    }

    override fun disconnect() {
        connectionCallback?.invoke(false)
        // Don't notify false here as it may cause UI flicker
        socket?.disconnect()

        // DON'T remove all event listeners - just unregister message callback
        // socket?.off() <- This is the problem
    }

    override fun registerMessageListener(onMessageReceived: (Chat) -> Unit) {
        messageCallback = onMessageReceived
    }

    override fun unregisterMessageListener() {
        messageCallback = null
    }

    override fun sendMessage(chat: Chat) {
        Timber.d("Sending message: $chat")
        val jsonStr = Gson().toJson(chat)
        socket?.emit(CHAT_KEYS.NEW_MESSAGE, jsonStr)
    }

    companion object {
        // For Genymotion
        private const val SOCKET_URL = "http://10.0.3.2:3000/"

        private object CHAT_KEYS {
            const val NEW_MESSAGE = "new_message"
            const val BROADCAST = "broadcast"
        }
    }
}