package com.example.socketchatmessagesincompose.data.repository

import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.ui.model.ChatHistoryResponse
import io.socket.client.Socket


interface SocketManager {
    fun connect(onConnectionChange: (Boolean) -> Unit)
    fun disconnect()
    fun getSocket(): Socket?
    fun sendMessage(chat: Chat)
    fun registerMessageListener(onMessageReceived: (Chat) -> Unit)
    fun unregisterMessageListener()
    fun fetchMessageHistory(
        page: Int,
        pageSize: Int,
        onHistoryReceived: (ChatHistoryResponse) -> Unit
    )
}