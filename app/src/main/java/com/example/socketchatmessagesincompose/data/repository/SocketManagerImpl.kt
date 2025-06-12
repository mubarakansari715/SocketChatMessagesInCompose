package com.example.socketchatmessagesincompose.data.repository

import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.ui.model.ChatHistoryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private var historyCallback: ((ChatHistoryResponse) -> Unit)? = null

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

        // Handle message history response
        socket.on(CHAT_KEYS.MESSAGE_HISTORY) { args ->
            args?.let { d ->
                if (d.isNotEmpty()) {
                    try {
                        val jsonStr = d[0].toString()
                        Timber.d("Received history response: $jsonStr")

                        val type = object : TypeToken<ChatHistoryResponse>() {}.type
                        val response = Gson().fromJson<ChatHistoryResponse>(jsonStr, type)

                        Timber.d("Parsed history: ${response.messages.size} messages, page=${response.page}, hasMore=${response.hasMore}")
                        historyCallback?.invoke(response)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing message history: ${e.message}")
                        historyCallback?.invoke(
                            ChatHistoryResponse(
                                messages = emptyList(),
                                totalMessagesCount = 0,
                                page = 0,
                                pageSize = 0,
                                hasMore = false
                            )
                        )
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

    override fun fetchMessageHistory(page: Int, pageSize: Int, onHistoryReceived: (ChatHistoryResponse) -> Unit) {
       /* historyCallback = onHistoryReceived

        val requestData = mapOf(
            "page" to page,
            "pageSize" to pageSize
        )

        Timber.d("Requesting history: page=$page, pageSize=$pageSize")
        socket.emit(CHAT_KEYS.FETCH_HISTORY, Gson().toJson(requestData))*/


        //Testing only
        // Store the callback
        historyCallback = onHistoryReceived

        // Define total available messages in the system
        val totalAvailableMessages = 100

        // Calculate proper pagination values
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, totalAvailableMessages)
        val actualPageSize = maxOf(0, endIndex - startIndex) // Handle case where page is beyond available data
        val hasMoreMessages = endIndex < totalAvailableMessages

        // Create a list of 100 chat items with sequential numbering
        val dummyMessages = if (startIndex < totalAvailableMessages) {
            List(actualPageSize) { index ->
                // Simple sequential numbering - no reverse logic
                val messageNum = startIndex + index + 1
                val user = when (messageNum % 3) {
                    0 -> "Alice"
                    1 -> "Bob"
                    else -> "Happy"
                }

                // Simple timestamp - earlier messages have earlier timestamps
                val baseTimestamp = System.currentTimeMillis() - ((totalAvailableMessages - messageNum) * 60000L)

                Chat(
                    username = user,
                    text = "Test message #$messageNum",
                    timestamp = baseTimestamp,
                    id = "msg_${messageNum}"
                )
            }
        } else {
            emptyList() // Return empty list if page is out of bounds
        }

        // Create response with proper pagination metadata
        val response = ChatHistoryResponse(
            messages = dummyMessages,
            totalMessagesCount = totalAvailableMessages,
            page = page,
            pageSize = pageSize,
            hasMore = hasMoreMessages
        )

        Timber.d("TEST: Page $page (items $startIndex-${endIndex-1}) " +
                "of $totalAvailableMessages total messages. Has more: $hasMoreMessages")

        // Simulate network delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            historyCallback?.invoke(response)
        }, 500)
    }

    companion object {

        private object CHAT_KEYS {
            const val NEW_MESSAGE = "new_message"
            const val BROADCAST = "broadcast"
            const val FETCH_HISTORY = "fetch_history"
            const val MESSAGE_HISTORY = "message_history"
        }
    }
}