package com.example.socketchatmessagesincompose.ui.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socketchatmessagesincompose.data.repository.ChatRepository
import com.example.socketchatmessagesincompose.ui.model.Chat
import com.example.socketchatmessagesincompose.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

sealed class ChatUiState {
    object Connecting : ChatUiState()
    object Connected : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

data class ChatUiEvent(
    val scrollToBottom: Boolean = false,
    val showConnectionError: Boolean = false, // for new message
    val maintainScrollPosition: Boolean = false  // New flag for pagination
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var isLoadingMore = mutableStateOf(false)
    var totalMessagesCount = mutableIntStateOf(0)

    // State containing messages and UI state
    data class ChatState(
        val messages: List<Chat> = emptyList(),
        val isLoading: Boolean = false,
        val isConnected: Boolean = false,
        val currentPage: Int = 0,
        val hasMoreMessages: Boolean = false,
        val error: String? = null,
    )

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Connecting)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<Chat>>(emptyList())
    val chatMessages: StateFlow<List<Chat>> = _chatMessages.asStateFlow()

    // For UI events that need to be consumed once
    private val _uiEvent = MutableStateFlow(ChatUiEvent())
    val uiEvent: StateFlow<ChatUiEvent> = _uiEvent.asStateFlow()

    // Call this after consuming the event
    fun clearEvents() {
        _uiEvent.value = ChatUiEvent()
    }

    private var currentPage = 0
    private val pageSize = 20

   /* init {
        connectSocket()
    }*/

    fun connectSocket() {
        chatRepository.connectSocket { isConnected ->
            _uiState.value = if (isConnected) ChatUiState.Connected else ChatUiState.Connecting
            _state.update { it.copy(isConnected = isConnected) }

            // When connected, load initial messages and setup listeners
            if (isConnected) {
                loadInitialMessages()
                listenForMessages()
            }
        }
    }

    private fun loadInitialMessages() {
        Timber.d("Loading initial messages")
        _state.update { it.copy(isLoading = true) }

        chatRepository.getMessageHistory(0, 25) { response ->
            Timber.d("Loaded initial ${response.messages.size} messages, hasMore=${response.hasMore}")
            totalMessagesCount.intValue = response.totalMessagesCount
            _state.update { state ->
                state.copy(
                    messages = response.messages,
                    isLoading = false,
                    currentPage = response.page,
                    hasMoreMessages = response.hasMore
                )
            }
            _chatMessages.value = response.messages
        }
    }

    fun loadMoreMessages() {
        if (isLoadingMore.value) return
        isLoadingMore.value = true

        val nextPage = _state.value.currentPage + 1
        Timber.d("Loading more messages, page: $nextPage")
        _state.update { it.copy(isLoading = true) }

        chatRepository.getMessageHistory(nextPage, 25) { response ->
            Timber.d("Loaded additional ${response.messages.size} messages")
//            val updatedMessages = response.messages + _state.value.messages --> for added new message on top
            val updatedMessages = _state.value.messages + response.messages
            _state.update { state ->
                state.copy(
                    // Prepend older messages at the top
                    messages = updatedMessages,
                    isLoading = false,
                    currentPage = response.page,
                    hasMoreMessages = response.hasMore
                )
            }
            isLoadingMore.value = false
            _chatMessages.value = updatedMessages
        }
    }

    private fun listenForMessages() {
        chatRepository.listenForMessages { chat ->
            // Add new message to the beginning of the list (position zero)
            val updatedMessages = listOf(chat) + _chatMessages.value
            _chatMessages.value = updatedMessages

            // Update the state
            _state.update { state ->
                state.copy(messages = updatedMessages)
            }

            // Trigger scroll to bottom for new messages
            _uiEvent.value = _uiEvent.value.copy(scrollToBottom = true)
        }
    }

    fun disconnectSocket() {
        chatRepository.disconnectSocket()
        chatRepository.stopListeningForMessages()
        _chatMessages.value = emptyList() // Clear messages when disconnected
        _state.update { ChatState() }
    }

    fun setUsername(username: String) {
        sessionManager.saveUserSession(
            userId = username,
            token = UUID.randomUUID().toString()
        )
        _username.value = username
    }

    fun setMessageInput(message: String) {
        _messageInput.value = message
    }

    fun sendMessage() {
        val message = _messageInput.value.trim()
        val username = _username.value.trim()

        if (message.isEmpty() || username.isEmpty()) return

        if (!_state.value.isConnected) {
            _uiEvent.value = _uiEvent.value.copy(showConnectionError = true)
            return
        }

        chatRepository.sendMessage(username, message)
        _messageInput.value = ""
    }

    override fun onCleared() {
        disconnectSocket()
        super.onCleared()
    }
}