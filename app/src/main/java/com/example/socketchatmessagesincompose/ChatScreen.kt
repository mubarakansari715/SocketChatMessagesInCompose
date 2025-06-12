package com.example.socketchatmessagesincompose

import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun ChatScreen(
    username: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle(emptyList())
    val messageInput by viewModel.messageInput.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Set username for the chat session
    LaunchedEffect(username) {
        viewModel.setUsername(username)
    }

    // Connect to socket when screen opens
    LaunchedEffect(Unit) {
        viewModel.connectSocket()
    }

    // Disconnect when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectSocket()
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Connection status bar
        if (uiState is ChatUiState.Connecting) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Connecting...",
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Chat messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            items(chatMessages) { chat ->
                val isSelf = chat.username == username
                if (isSelf) {
                    SelfChatBubble(chat)
                } else {
                    OtherChatBubble(chat)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageInput,
                onValueChange = { viewModel.setMessageInput(it) },
                placeholder = { Text("Type a message") },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.sendMessage() },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (messageInput.isNotEmpty()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SelfChatBubble(chat: Chat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 64.dp, end = 8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "You",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = RoundedCornerShape(8.dp, 0.dp, 8.dp, 8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            shadowElevation = 1.dp
        ) {
            Text(
                text = chat.text,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun OtherChatBubble(chat: Chat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 64.dp, start = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = chat.username,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            shape = RoundedCornerShape(0.dp, 8.dp, 8.dp, 8.dp),
            color = Color.LightGray,
            shadowElevation = 1.dp
        ) {
            Text(
                text = chat.text,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}