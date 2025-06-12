package com.example.socketchatmessagesincompose.data.repository

import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.ui.model.ChatHistoryResponse
import javax.inject.Inject
import javax.inject.Singleton


class ChatRepositoryImpl @Inject constructor(
    private val socketManager: SocketManager
) : ChatRepository {

    override fun connectSocket(onConnectionChange: (Boolean) -> Unit) {
        socketManager.connect(onConnectionChange)
    }

    override fun disconnectSocket() {
        socketManager.disconnect()
    }

    override fun sendMessage(username: String, message: String) {
        val chat = Chat(username = username, text = message)
        socketManager.sendMessage(chat)
    }

    override fun listenForMessages(onMessageReceived: (Chat) -> Unit) {
        socketManager.registerMessageListener(onMessageReceived)
    }

    override fun stopListeningForMessages() {
        socketManager.unregisterMessageListener()
    }

    override fun getMessageHistory(page: Int, pageSize: Int, onHistoryReceived: (ChatHistoryResponse) -> Unit) {
        socketManager.fetchMessageHistory(page, pageSize, onHistoryReceived)
    }
}