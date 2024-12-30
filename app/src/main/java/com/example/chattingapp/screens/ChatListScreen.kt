package com.example.chattingapp.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chattingapp.LCViewModel

@Composable
fun ChatListScreen(navController: NavController,vm:LCViewModel) {
    BottomNavigationMenu(BottomNavigationItem.CHATLIST,navController)
    Text(
        text = "Create Account",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Serif,
        color = Color(0xFF2E7D32) // Green shade
    )
}