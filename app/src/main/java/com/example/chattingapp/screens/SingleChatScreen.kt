package com.example.chattingapp.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chattingapp.CommonDivider
import com.example.chattingapp.CommonImage
import com.example.chattingapp.LCViewModel
import com.example.chattingapp.data.Message

// SingleChatScreen - Displays the main chat screen layout.
// Includes the chat header, reply box, and handles message sending.
@Composable
fun SingleChatScreen(
    navController: NavController, // Navigation controller for handling back navigation
    vm: LCViewModel, // ViewModel to handle chat logic
    chatId: String // ID of the current chat
) {
    // State to manage the reply text input
    var reply by rememberSaveable {
        mutableStateOf("")
    }

    // Callback for sending a reply
    val onSentReply = {
        vm.onSendReply(chatId, reply)
        reply = "" // Clear the input field after sending
    }

    // Retrieve user and chat information
    val myUser = vm.userData.value
    var currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
    var chatMessage=vm.chatMessages
    LaunchedEffect(key1 = Unit) {
        vm.populateMessages(chatId)
    }
    BackHandler {
        vm.dePopulateMessage()
    }

    // Main layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 37.dp)
    ) {
        // Chat header at the top
        ChatHeader(name = chatUser.userName ?: "", imageUrl = chatUser.image ?: "") {
            navController.popBackStack()
            vm.dePopulateMessage()
        }

        Spacer(modifier = Modifier.weight(1f)) // Spacer to push reply box to the bottom

        if (myUser != null) {
            MessageBox(modifier = Modifier.padding(8.dp), chatMessage =chatMessage.value,myUser.userId?:"" )
        }

        // Reply box for composing and sending messages
        ReplyBox(
            onReplyChange = { reply = it },
            onSentReply = onSentReply,
            reply = reply
        )
    }
}


@Composable
fun ReplyBox(
    onReplyChange: (String) -> Unit,
    onSentReply: () -> Unit,
    reply: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .shadow(2.dp, RoundedCornerShape(8.dp))
            .padding(3.dp)

        //.imePadding() // Adjust for keyboard visibility
    ) {
        // Classic Divider
        Divider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field for composing a reply
            OutlinedTextField(
                value = reply,
                onValueChange = onReplyChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = {
                    Text(
                        text = "Type a message...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                },
                shape = RoundedCornerShape(8.dp),
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            // Classic Send Button
            IconButton(
                onClick = {
                    if (reply.isNotBlank()) {
                        onSentReply()
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .size(48.dp)
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

// ChatHeader - Displays the chat header at the top of the screen with user details and a back button.
// It includes a profile picture, user name, and optional online status.
@Composable
fun ChatHeader(
    name: String, // The name of the chat user
    imageUrl: String?, // The URL of the user's profile image
    onBack: () -> Unit // Callback for handling back button press
) {
    // State to manage header expansion for showing additional details
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary) // Classic solid color header
            .clickable { isExpanded = !isExpanded } // Toggle expansion state on click
            .animateContentSize() // Smooth transition for content size changes
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button for navigation
        Icon(
            Icons.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier
                .clickable { onBack.invoke() }
                .padding(8.dp)
                .size(30.dp)
        )

        // Profile Image with circular shape and shadow
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape) // Circular profile image
                .background(Color.White) // White background for contrast
                .shadow(4.dp, CircleShape) // Shadow for elevation effect
        )

        Spacer(modifier = Modifier.width(12.dp))

        // User details including name and optional status
        Column(modifier = Modifier.animateContentSize()) {
            // Display user name
            Text(
                text = name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Display online status when expanded
            if (isExpanded) {
                Text(
                    text = "Online", // Placeholder status, can be dynamic
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
@Composable
fun MessageBox(modifier: Modifier, chatMessage: List<Message>, currentUserId: String) {
    LazyColumn(modifier = modifier.padding(8.dp)) {
        items(chatMessage) { msg ->
            val isCurrentUser = msg.sendBy == currentUserId
            val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
            val backgroundColor = if (isCurrentUser) Color(0xFF00BFAE) else Color(0xFF6200EE)
            val textColor = Color.White

            val messageState = remember { MutableTransitionState(false) }
            LaunchedEffect(msg) {
                messageState.targetState = true
            }

            AnimatedVisibility(
                visibleState = messageState,
                enter = slideInHorizontally(
                    initialOffsetX = { if (isCurrentUser) it else -it },
                    animationSpec = tween(durationMillis = 500)
                ) + fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                            .widthIn(max = 300.dp)
                    ) {
                        Text(
                            text = msg.message ?: "",
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}



