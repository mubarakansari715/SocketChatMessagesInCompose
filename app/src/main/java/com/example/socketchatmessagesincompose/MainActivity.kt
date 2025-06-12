package com.example.socketchatmessagesincompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.socketchatmessagesincompose.ui.theme.SocketChatMessagesInComposeTheme
import dagger.hilt.android.AndroidEntryPoint

object ChatDestinations {
    const val USERNAME_ROUTE = "username"
    const val CHAT_ROUTE = "chat/{username}"

    fun createChatRoute(username: String): String = "chat/$username"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocketChatMessagesInComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    Scaffold { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = ChatDestinations.USERNAME_ROUTE,
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            // Username screen
                            composable(ChatDestinations.USERNAME_ROUTE) {
                                UserNameScreen(
                                    navigateToChat = { username ->
                                        navController.navigate(
                                            ChatDestinations.createChatRoute(username)
                                        )
                                    },
                                    modifier = Modifier.focusModifier()
                                )
                            }

                            // Chat screen
                            composable(
                                route = ChatDestinations.CHAT_ROUTE,
                                arguments = listOf(
                                    navArgument("username") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val username = backStackEntry.arguments?.getString("username") ?: ""
                                ChatScreen(
                                    username = username
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserNameScreen(modifier: Modifier, navigateToChat: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    val isButtonEnabled = username.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter Your Username",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        Button(
            onClick = { navigateToChat(username) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = isButtonEnabled
        ) {
            Text("Proceed to Chat")
        }
    }
}

