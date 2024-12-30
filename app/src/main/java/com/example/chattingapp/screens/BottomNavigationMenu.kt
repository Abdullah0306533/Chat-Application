package com.example.chattingapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chattingapp.LCViewModel
import com.example.chattingapp.R
import com.example.chattingapp.ScreenDestinations

// Enum for Bottom Navigation Items with Fancy Names
enum class BottomNavigationItem(
    val icon: Int,
    val destination: ScreenDestinations,
    val label: String
) {
    CHATLIST(R.drawable.chat2, ScreenDestinations.ChatList, "ChitChat"),
    STATUSLIST(R.drawable.status, ScreenDestinations.StatusList, "BuzzZone"),
    PROFILE(R.drawable.profile, ScreenDestinations.Profile, "MySpace")
}

@Composable
fun BottomNavigationMenu(
    selectedItem: BottomNavigationItem,
    navController: NavController
) {
    NavigationBar(
        containerColor = Color(0xFFF5F5F5), // Light gray background
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        BottomNavigationItem.values().forEach { item ->
            val isSelected = item == selectedItem

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            tint = Color(0xFF757575)
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.destination.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF757575),
                    unselectedIconColor = Color(0xFF757575)
                )
            )
        }
    }
}

