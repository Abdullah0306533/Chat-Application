package com.example.chattingapp.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.chattingapp.LCViewModel

@Composable
fun StatusScreen(navController:NavController,vm:LCViewModel) {
    BottomNavigationMenu(BottomNavigationItem.STATUSLIST,navController)

}