package com.example.chattingapp.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.chattingapp.CommonProgressBar
import com.example.chattingapp.CommonRow
import com.example.chattingapp.LCViewModel
import com.example.chattingapp.ScreenDestinations
import com.example.chattingapp.TitleText
import com.example.chattingapp.navigateTo

@Composable
fun ChatListScreen(navController: NavController, vm: LCViewModel) {
    val inProgress by vm.inProcessChat
    val chats by vm.chats
    val userData by vm.userData
    val showDialog = remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FAB(
                showDialog = showDialog.value,
                onFabClick = { showDialog.value = true },
                onDismiss = { showDialog.value = false },
                onAddChat = {
                    vm.onAddChat(it)
                    showDialog.value = false
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                TitleText("Chats")

                if (inProgress) {
                    CommonProgressBar()
                } else if (chats.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Chats Available", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 60.dp)
                    ) {
                        items(chats) { chat ->
                            val chatUser = if (chat.user1.userId == userData?.userId) chat.user2 else chat.user1

                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(500)),
                                exit = fadeOut(tween(500))
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .animateContentSize(),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    CommonRow(
                                        imageUrl = chatUser.image,
                                        name = chatUser.userName
                                    ) {
                                        chat.chatId?.let {
                                            navigateTo(navController, ScreenDestinations.SingleChat.createRoute(it))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(56.dp))
            }
        },
        bottomBar = {
            BottomNavigationMenu(BottomNavigationItem.CHATLIST, navController)
        }
    )
}

@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val onAddChatNumber = remember { mutableStateOf("") }

    if (showDialog) {
        Dialog(
            onDismissRequest = { onDismiss.invoke() },
        ) {
            Surface(
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Add Chat",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = onAddChatNumber.value,
                        onValueChange = { onAddChatNumber.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("Enter number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ElevatedButton(
                            onClick = {
                                onDismiss.invoke()
                                onAddChatNumber.value = ""
                            }
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        ElevatedButton(
                            onClick = {
                                if (onAddChatNumber.value.isNotBlank()) {
                                    onAddChat(onAddChatNumber.value.trim())
                                    onAddChatNumber.value = ""
                                }
                            }
                        ) {
                            Text("Add Chat")
                        }
                    }
                }
            }
        }
    }

    FloatingActionButton(
        onClick = { onFabClick() },
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier
            .padding(bottom = 80.dp)
            .animateContentSize()
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Chat", tint = Color.White)
    }
}