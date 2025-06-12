package com.example.socketchatmessagesincompose.data.repository

import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.ui.model.ChatHistoryResponse


interface SocketManager {
    fun connect(onConnectionChange: (Boolean) -> Unit)
    fun disconnect()
    fun sendMessage(chat: Chat)
    fun registerMessageListener(onMessageReceived: (Chat) -> Unit)
    fun unregisterMessageListener()
    fun fetchMessageHistory(page: Int, pageSize: Int, onHistoryReceived: (ChatHistoryResponse) -> Unit)
}