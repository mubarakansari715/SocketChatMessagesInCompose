package com.example.socketchatmessagesincompose.ui.model

data class ChatHistoryResponse(
    val messages: List<Chat> = emptyList(),
    val totalMessagesCount: Int = 0,
    val page: Int = 0,
    val pageSize: Int = 0,
    val hasMore: Boolean = false
)