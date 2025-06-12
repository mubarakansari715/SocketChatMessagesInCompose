package com.example.socketchatmessagesincompose.utils

object ChatDestinations {
    const val USERNAME_ROUTE = "username"
    const val CHAT_ROUTE = "chat/{username}"

    fun createChatRoute(username: String): String = "chat/$username"
}