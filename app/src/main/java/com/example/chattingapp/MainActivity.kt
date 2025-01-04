package com.example.chattingapp

import ProfileScreen
import SignUpScreen
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chattingapp.data.ToastUtil
import com.example.chattingapp.screens.ChatListScreen
import com.example.chattingapp.screens.LoginScreen
import com.example.chattingapp.screens.SingleChatScreen
import com.example.chattingapp.screens.SingleStatusScreen
import com.example.chattingapp.screens.StatusScreen
import com.example.chattingapp.ui.theme.ChattingAppTheme
import dagger.hilt.android.AndroidEntryPoint

// Sealed class for app screen destinations
sealed class ScreenDestinations(val route: String) {
    object SignUp : ScreenDestinations("signup")
    object Login : ScreenDestinations("login")
    object Profile : ScreenDestinations("profile")
    object ChatList : ScreenDestinations("chatList")
    object SingleChat : ScreenDestinations("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"
    }
    object StatusList : ScreenDestinations("statusList")
    object SingleStatus : ScreenDestinations("singleStatus/{userId}") {
        fun createRoute(id: String) = "singleStatus/$id"
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable modern edge-to-edge design
        enableEdgeToEdge()

        setContent {
            ChattingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation() // Set up navigation
                    ToastUtil.init(this)
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {
        // Initialize NavController
        val navController = rememberNavController()

        // Fetch ViewModel inside composable
        val vm = hiltViewModel<LCViewModel>()

        // Determine start destination based on sign-in state
        val startDestination = if (!vm.signIn.value) {
            ScreenDestinations.SignUp.route
        } else {
            ScreenDestinations.ChatList.route
        }

        // Navigation host
        NavHost(navController = navController, startDestination = startDestination) {

            // SignUp Screen
            composable(ScreenDestinations.SignUp.route) {
                SignUpScreen(navController, vm)
            }

            // Login Screen
            composable(ScreenDestinations.Login.route) {
                LoginScreen(vm, navController)
            }

            // Profile Screen
            composable(ScreenDestinations.Profile.route) {
                ProfileScreen(navController, vm, applicationContext)
            }

            // Chat List Screen
            composable(ScreenDestinations.ChatList.route) {
                ChatListScreen(navController, vm)
            }

            // Status List Screen
            composable(ScreenDestinations.StatusList.route) {
                StatusScreen(navController, vm)
            }

            // Single Chat Screen
            composable(ScreenDestinations.SingleChat.route) {
                val chatId = it.arguments?.getString("chatId")
                chatId?.let { id ->
                    SingleChatScreen(navController, vm, id)
                }
            }

            // Single Status Screen
            composable(ScreenDestinations.SingleStatus.route) {
                SingleStatusScreen()
            }
        }
    }
}
