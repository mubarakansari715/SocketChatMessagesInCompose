package com.example.socketchatmessagesincompose.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.example.socketchatmessagesincompose.data.config.AppConfig.SOCKET_URL
import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.ui.model.ChatHistoryResponse
import com.example.socketchatmessagesincompose.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import timber.log.Timber
import java.net.URISyntaxException
import javax.inject.Inject
import javax.inject.Singleton


/*
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
       */
/* historyCallback = onHistoryReceived

        val requestData = mapOf(
            "page" to page,
            "pageSize" to pageSize
        )

        Timber.d("Requesting history: page=$page, pageSize=$pageSize")
        socket.emit(CHAT_KEYS.FETCH_HISTORY, Gson().toJson(requestData))*//*



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
}*/


class SocketManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) : SocketManager {

    private lateinit var socket: Socket

    private var connectionCallback: ((Boolean) -> Unit)? = null
    private var messageCallback: ((Chat) -> Unit)? = null
    private var historyCallback: ((ChatHistoryResponse) -> Unit)? = null

    override fun connect(onConnectionChange: (Boolean) -> Unit) {
        val userId = sessionManager.getUserId()
        val authToken = sessionManager.getAuthToken()
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        //val appVersion = BuildConfig.VERSION_NAME
        val appVersion = "1.0.0"

        val options = IO.Options().apply {
            reconnection = true                       // Enable automatic reconnection
            reconnectionAttempts = 10                 // Try reconnecting 10 times before giving up
            reconnectionDelay = 1000                  // Wait 1 second between each attempt
            timeout =
                10000                           // 10-second timeout for the initial connection attempt
            forceNew = true                           // Always create a new Manager instance
            secure = true                             // Use HTTPS/WSS connection
            transports = arrayOf("websocket")         // Use WebSocket only (disable polling for production)
            rememberUpgrade =
                true                    // Skip HTTP polling if previously upgraded to WebSocket

            // Dummy authentication and user metadata
            query = "userId=$userId&authToken=$authToken&device=$deviceId&appVersion=$appVersion"

            // Optional headers (if supported by server)
            extraHeaders = mapOf(
                "Authorization" to listOf("Bearer $authToken"),
                "X-Device-ID" to listOf(deviceId),
                "X-App-Version" to listOf(appVersion)
            )
        }

        try {
            Timber.e("@@@options $options")
            socket = IO.socket(SOCKET_URL, options)
        } catch (e: Exception) {
            Timber.e(e, "Socket initialization failed")
            onConnectionChange(false)
            return
        }

        connectionCallback = onConnectionChange
        setupListeners()
        socket.connect()
    }

    override fun disconnect() {
        connectionCallback?.invoke(false)
        if (::socket.isInitialized) socket.disconnect()
    }

    override fun getSocket(): Socket? = if (::socket.isInitialized) socket else null

    override fun registerMessageListener(onMessageReceived: (Chat) -> Unit) {
        messageCallback = onMessageReceived
    }

    override fun unregisterMessageListener() {
        messageCallback = null
    }

    override fun sendMessage(chat: Chat) {
        socket.emit(CHAT_KEYS.NEW_MESSAGE, Gson().toJson(chat))
    }

    private fun setupListeners() {
        socket.on(Socket.EVENT_CONNECT) {
            Timber.d("Socket connected")
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

        socket.on(CHAT_KEYS.BROADCAST) { args ->
            args?.firstOrNull()?.toString()?.let {
                try {
                    val chat = Gson().fromJson(it, Chat::class.java)
                    messageCallback?.invoke(chat)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse incoming message")
                }
            }
        }

        socket.on(CHAT_KEYS.MESSAGE_HISTORY) { args ->
            args?.firstOrNull()?.toString()?.let {
                try {
                    val type = object : TypeToken<ChatHistoryResponse>() {}.type
                    val history = Gson().fromJson<ChatHistoryResponse>(it, type)
                    historyCallback?.invoke(history)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse message history")
                    historyCallback?.invoke(ChatHistoryResponse(emptyList(), 0, 0, 0, false))
                }
            }
        }
    }

    override fun fetchMessageHistory(
        page: Int,
        pageSize: Int,
        onHistoryReceived: (ChatHistoryResponse) -> Unit
    ) {

        /* historyCallback = onHistoryReceived

        val requestData = mapOf(
            "page" to page,
            "pageSize" to pageSize
        )

        Timber.d("Requesting history: page=$page, pageSize=$pageSize")
        socket.emit(CHAT_KEYS.FETCH_HISTORY, Gson().toJson(requestData))*/

        historyCallback = onHistoryReceived

        // Dummy pagination logic
        val total = 100
        val start = page * pageSize
        val end = (start + pageSize).coerceAtMost(total)
        val hasMore = end < total

        val messages = (start until end).map {
            Chat(
                username = listOf("Alice", "Bob", "Happy")[it % 3],
                text = "Message #${it + 1}",
                timestamp = System.currentTimeMillis() - ((total - it) * 60000L),
                id = "msg_${it + 1}"
            )
        }

        val response = ChatHistoryResponse(
            messages = messages,
            totalMessagesCount = total,
            page = page,
            pageSize = pageSize,
            hasMore = hasMore
        )

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
