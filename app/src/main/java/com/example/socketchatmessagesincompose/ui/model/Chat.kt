package com.example.socketchatmessagesincompose.ui.model

data class Chat(
    var id: String = "",
    val username: String,
    val text: String,
    var isSelf: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
)