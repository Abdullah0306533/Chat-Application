package com.example.chattingapp.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chattingapp.CommonImage
import com.example.chattingapp.LCViewModel
import com.example.chattingapp.data.Message
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SingleChatScreen(
    navController: NavController,
    vm: LCViewModel,
    chatId: String
) {
    var reply by rememberSaveable { mutableStateOf("") }
    val onSentReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }

    val focusRequester = remember { FocusRequester() }
    val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser = if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
    val chatMessages = vm.chatMessages

    val scrollState = rememberLazyListState()

    LaunchedEffect(chatMessages.value.size) {
        vm.populateMessages(chatId)
        scrollState.scrollToItem(0)
    }

    BackHandler { vm.dePopulateMessage()
        navController.popBackStack()}

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
    ) {
        ChatHeader(name = chatUser.userName ?: "", imageUrl = chatUser.image ?: "") {
            navController.popBackStack()
            vm.dePopulateMessage()
        }

        MessageBox(
            modifier = Modifier.weight(1f).padding(8.dp),
            chatMessages.value,
            myUser?.userId ?: "",
            scrollState
        )

        ReplyBox(
            onReplyChange = { reply = it },
            onSentReply = onSentReply,
            reply = reply,
            focusRequester = focusRequester
        )
    }
}

@Composable
fun MessageBox(modifier: Modifier, chatMessages: List<Message>, currentUserId: String, scrollState: LazyListState) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        reverseLayout = true
    ) {
        items(chatMessages.reversed()) { msg ->
            val isCurrentUser = msg.sendBy == currentUserId
            val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
            val backgroundColor = if (isCurrentUser) Color(0xFF00BFAE) else Color(0xFF6200EE)
            val textColor = Color.White

            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val time = sdf.format(Date(msg.timeStamp))

            val messageState = remember { MutableTransitionState(false) }
            LaunchedEffect(msg) {
                messageState.targetState = true
            }

            AnimatedVisibility(
                visibleState = messageState,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 500)
                ) + fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = fadeOut(animationSpec = tween(durationMillis = 500))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .background(backgroundColor, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                            .widthIn(max = 300.dp)
                    ) {
                        Column {
                            Text(msg.message ?: "", color = textColor, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(time, color = textColor.copy(0.7f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyBox(
    onReplyChange: (String) -> Unit,
    onSentReply: () -> Unit,
    reply: String,
    focusRequester: FocusRequester
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val imeState = remember { mutableStateOf(false) }
    val sendButtonScale = animateFloatAsState(
        targetValue = if (reply.isNotBlank()) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding() // Ensures the ReplyBox moves above the keyboard
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = reply,
                onValueChange = onReplyChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                placeholder = { Text("Type a message...") },
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (reply.isNotBlank()) {
                        onSentReply()
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .scale(sendButtonScale.value)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}


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
