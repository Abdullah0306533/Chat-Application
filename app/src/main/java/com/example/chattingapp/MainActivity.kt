package com.example.chattingapp

// Created and optimized by OpenAI's ChatGPT
// Assistance provided for permissions handling and navigation structure refinement

import ProfileScreen
import SignUpScreen
import android.content.Intent
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chattingapp.screens.ChatListScreen
import com.example.chattingapp.screens.LoginScreen
import com.example.chattingapp.screens.SingleChatScreen
import com.example.chattingapp.screens.SingleStatusScreen
import com.example.chattingapp.screens.StatusScreen
import com.example.chattingapp.ui.theme.ChattingAppTheme
import dagger.hilt.android.AndroidEntryPoint

// Sealed class for app screen destinations
sealed class ScreenDestinations(val route: String) {
    object SignUp : ScreenDestinations("signup")         // Signup screen
    object Login : ScreenDestinations("login")           // Login screen
    object Profile : ScreenDestinations("profile")       // Profile screen
    object ChatList : ScreenDestinations("chatList")     // Chat list screen
    object SingleChat : ScreenDestinations("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"   // Generate route with chatId
    }

    object StatusList : ScreenDestinations("statusList") // Status list screen
    object SingleStatus : ScreenDestinations("singleStatus/{userId}") {
        fun createRoute(id: String) = "singleStatus/$id" // Generate route with userId
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()  // Check and request permissions on app launch
        enableEdgeToEdge()  // Enables modern edge-to-edge design.

        setContent {
            ChattingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Background color from theme
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation() {
        // Navigation logic
        val navController = rememberNavController()
        val vm = hiltViewModel<LCViewModel>()

        NavHost(navController = navController, startDestination = ScreenDestinations.SignUp.route) {

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
                ProfileScreen(navController = navController, vm, applicationContext)
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
            composable(ScreenDestinations.SingleChat.route) { backStackEntry ->
                SingleChatScreen()
            }

            // Single Status Screen
            composable(ScreenDestinations.SingleStatus.route) { backStackEntry ->
                SingleStatusScreen()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        // Check if all permissions are granted
        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            // All permissions are granted; no action needed
            return
        }

        val shouldShowRationale = permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }

        if (shouldShowRationale) {
            // Request permissions normally if they haven't been permanently denied
            requestPermissionsLauncher.launch(permissions)
        } else {
            // If permissions are permanently denied, navigate to settings
            val permanentlyDenied = permissions.any {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_DENIED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (permanentlyDenied) {
                showPermissionDeniedDialog()
            } else {
                // Request permissions for the first time
                requestPermissionsLauncher.launch(permissions)
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        Toast.makeText(this, "Permissions are required for app functionality. Please enable them in settings.", Toast.LENGTH_LONG).show()

        // Redirect to settings if permissions are permanently denied
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${packageName}")
        }
        startActivity(intent)
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.values.all { it }
            if (!allGranted) {
                // If any permission is denied, close the app (as you requested)
                finish()  // Ends the app
            }
        }
}
