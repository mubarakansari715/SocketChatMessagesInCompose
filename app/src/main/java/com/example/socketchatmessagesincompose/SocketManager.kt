package com.example.socketchatmessagesincompose


interface SocketManager {
    fun connect(onConnectionChange: (Boolean) -> Unit)
    fun disconnect()
    fun sendMessage(chat: Chat)
    fun registerMessageListener(onMessageReceived: (Chat) -> Unit)
    fun unregisterMessageListener()
}