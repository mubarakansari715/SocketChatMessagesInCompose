package com.example.socketchatmessagesincompose.data.repository

import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.ui.model.ChatHistoryResponse


interface ChatRepository {
    fun connectSocket(onConnectionChange: (Boolean) -> Unit)
    fun disconnectSocket()
    fun sendMessage(username: String, message: String)
    fun listenForMessages(onMessageReceived: (Chat) -> Unit)
    fun stopListeningForMessages()
    fun getMessageHistory(page: Int, pageSize: Int, onHistoryReceived: (ChatHistoryResponse) -> Unit)
}