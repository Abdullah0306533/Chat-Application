import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chattingapp.*
import com.example.chattingapp.screens.BottomNavigationItem
import com.example.chattingapp.screens.BottomNavigationMenu
import java.io.File

const val REQUEST_PERMISSION_CODE = 101

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel, context: Context) {
    val progress = vm.inProcess.value
    val userData = vm.userData.value

    var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
    var number by rememberSaveable { mutableStateOf(userData?.userNumber ?: "") }

    // Show progress animation
    AnimatedVisibility(visible = progress) {
        CommonProgressBar()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            BottomNavigationMenu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileContent(
                vm = vm,
                context = context,
                name = name,
                number = number,
                onBack = { navigateTo(navController,ScreenDestinations.ChatList.route) },
                onLogout = {
                    vm.signOut()
                    navigateTo(navController = navController,ScreenDestinations.Login.route)

                },
                onNumberChange = { number = it },
                onNameChange = { name = it },
                onSave={
                    vm.createOrUpdateProfile(name,number)
                }
            )
        }
    }
}

// Profile Content UI
@Composable
fun ProfileContent(
    vm: LCViewModel,
    context: Context,
    name: String,
    number: String,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val imagePath = vm.localImagePath.value

    // Scale animation for profile image
    val animatedScale = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        animatedScale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing)
        )
        animatedScale.animateTo(1f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Top Navigation Row (Back and Save)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                modifier = Modifier.clickable { onBack() },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = "Save",
                modifier = Modifier.clickable {
                    onSave()
                },
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Image with animation
        DisplayProfileImage(vm, animatedScale, context)

        Spacer(modifier = Modifier.height(16.dp))

        // Name Field
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        // Number Field
        OutlinedTextField(
            value = number,
            onValueChange = onNumberChange,
            label = { Text("Phone Number") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(
                text = "Logout",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// Display Profile Image
@Composable
fun DisplayProfileImage(vm: LCViewModel, animatedScale: Animatable<Float, AnimationVector1D>, context: Context) {
    val imagePath = vm.localImagePath.value
    val imagePicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.saveProfileImage(it, context) }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .scale(animatedScale.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier
                .padding(8.dp)
                .size(120.dp)
                .shadow(8.dp, CircleShape)
                .clickable { imagePicker.launch("image/*") }
        ) {
            imagePath?.let {
                val bitmap = BitmapFactory.decodeFile(it)
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } ?: run {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "Default Image"
                )
            }
        }
    }
}
