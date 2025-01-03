package com.example.chattingapp

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import coil3.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

// A utility function to navigate between screens using NavController
fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {
        // Ensure the destination is at the top of the stack
        popUpTo(route)
        // Prevent multiple instances of the same screen in the back stack
        launchSingleTop = true
    }
}

// A common composable that displays a semi-transparent progress indicator
@Composable
fun CommonProgressBar() {
    Row(
        modifier = Modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) {}
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Load Lottie animation from raw folder
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.loading_animation) // Replace with raw resource
        )

        val progress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(100.dp) // Customize size as needed
        )
    }
}

@Composable
fun CheckSignIn(vm: LCViewModel, navController: NavController) {
    val alreadySignIn = remember { mutableStateOf(false) }
    val signedIn = vm.signIn.value
    if (signedIn && !alreadySignIn.value) {
        alreadySignIn.value = true
        navController.navigate(ScreenDestinations.ChatList.route) {
            popUpTo(0)
        }

    }
}

@Composable
fun CommonDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 2.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 10.dp)

    )
}

@Composable
fun CommonImage(
    data: String?, modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {
    val painter = rememberAsyncImagePainter(data)
    Image(
        painter = painter,
        contentDescription = "",
        modifier = modifier,
        contentScale = contentScale
    )
}

// Function to save image locally
// Function to check and request permissions
/*fun hasStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        ActivityCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Permissions are automatically granted before API 23
    }
}
*/
/* Function to save image locally
fun saveImageToLocalStorage(context: Context, uri: Uri): String? {
    if (!hasStoragePermission(context)) {
        return null
    }

    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "ProfilePictures")

        // Create directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdirs()
        }

        // Generate unique filename
        val fileName = "profile_${UUID.randomUUID()}.jpg"
        val file = File(directory, fileName)

        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()

        return file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}*/



fun hasStoragePermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
}

@Composable
fun TitleText(text: String) {
    Text(text, fontSize = 35.sp, modifier = Modifier.padding(8.dp))
}
@Composable
fun CommonRow(imageUrl: String?, name: String?, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clickable { onItemClick.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .size(50.dp)
                .background(Color.Red)
        )
        Text(
            text = name ?: "Unknown User",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
    }
}


