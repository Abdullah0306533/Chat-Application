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
    val myUser = vm.userData.value // Get the current logged-in user
    val currentChat = vm.chats.value.first { it.chatId == chatId }

    // Correct identification of user1 and user2
    val chatUser = if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    // Fetch chat messages
    val chatMessages = vm.chatMessages

    // Set up lazy list state to control scrolling
    val scrollState = rememberLazyListState()

    // Ensure that messages are sorted by timestamp
    val sortedMessages = chatMessages.value.sortedBy { it.timeStamp }

    LaunchedEffect(chatMessages.value.size) {
        vm.populateMessages(chatId) // Populate messages when the screen is loaded
        scrollState.scrollToItem(0)  // Automatically scroll to the top
    }

    BackHandler {
        vm.dePopulateMessage()
        navController.popBackStack()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 8.dp)
    ) {
        // Header with user's profile and name
        ChatHeader(name = chatUser.userName ?: "", imageUrl = chatUser.image ?: "") {
            navController.popBackStack()
            vm.dePopulateMessage()
        }

        // MessageBox to display all the chat messages
        MessageBox(
            modifier = Modifier.weight(1f).padding(8.dp),
            chatMessages = sortedMessages, // Pass the sorted messages
            currentUserId =  myUser?.userId ?: "", // Pass the current user ID to differentiate messages
            scrollState = scrollState // Pass scroll state to MessageBox
        )

        // Reply box to send a message
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
    val isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(chatMessages.size) {
        isLoading.value = chatMessages.isEmpty()
    }

    if (isLoading.value) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.1f)
                .wrapContentHeight(Alignment.CenterVertically)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    LazyColumn(
        modifier = modifier.padding(8.dp),
        reverseLayout = true,  // This ensures the latest messages are at the bottom.
        state = scrollState
    ) {
        items(chatMessages.reversed()) { msg ->
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
                        .padding(vertical = 8.dp)
                        .wrapContentWidth(alignment), // Ensure alignment is applied correctly
                    verticalAlignment = Alignment.CenterVertically // Ensures the content is centered vertically within the row
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = backgroundColor,
                                shape = RoundedCornerShape(12.dp)  // Reduced corner radius
                            )
                            .shadow(0.dp, RoundedCornerShape(12.dp))  // Reduced shadow
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .widthIn(max = 320.dp)
                            .clip(RoundedCornerShape(12.dp))  // Consistent with reduced corner radius
                    ) {
                        Column {
                            Text(
                                text = msg.message ?: "",
                                color = textColor,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date(msg.timeStamp)),
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
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
