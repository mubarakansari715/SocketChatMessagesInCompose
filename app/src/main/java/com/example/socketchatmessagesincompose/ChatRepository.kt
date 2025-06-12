package com.example.socketchatmessagesincompose


interface ChatRepository {
    fun connectSocket(onConnectionChange: (Boolean) -> Unit)
    fun disconnectSocket()
    fun sendMessage(username: String, message: String)
    fun listenForMessages(onMessageReceived: (Chat) -> Unit)
    fun stopListeningForMessages()
}