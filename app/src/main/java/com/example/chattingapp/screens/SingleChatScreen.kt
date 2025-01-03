package com.example.chattingapp.screens



import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.navigation.NavController
import com.example.chattingapp.LCViewModel

@Composable
fun SingleChatScreen (navController: NavController,vm:LCViewModel,chatId:String){
    Text(chatId)
}