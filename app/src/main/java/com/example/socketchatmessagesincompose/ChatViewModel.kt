package com.example.socketchatmessagesincompose

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

sealed class ChatUiState {
    object Connecting : ChatUiState()
    object Connected : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Connecting)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Chat>>(emptyList())
    val chatMessages: StateFlow<List<Chat>> = _chatMessages.asStateFlow()

    init {
        listenForMessages()
    }

    fun connectSocket() {
        chatRepository.connectSocket { isConnected ->
            _uiState.value = if (isConnected) ChatUiState.Connected else ChatUiState.Connecting
        }
    }

    fun disconnectSocket() {
        chatRepository.disconnectSocket()
        chatRepository.stopListeningForMessages()
        _chatMessages.value = emptyList() // Clear messages when disconnected
    }

    private fun listenForMessages() {
        chatRepository.listenForMessages { chat ->
            _chatMessages.update { currentList -> currentList + chat }
        }
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setMessageInput(message: String) {
        _messageInput.value = message
    }

    fun sendMessage() {
        val message = _messageInput.value
        if (message.isNotEmpty() && _username.value.isNotEmpty()) {
            chatRepository.sendMessage(_username.value, message)
            _messageInput.value = ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectSocket()
    }
}