package com.example.chattingapp

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import com.example.chattingapp.data.Event
import com.example.chattingapp.data.USER_NODE
import com.example.chattingapp.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.uuid.Uuid

// ViewModel for managing user authentication and related state
@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    // Indicates whether an operation is in progress (e.g., loading state)
    var inProcess = mutableStateOf(false)

    // Holds events with a string message for state updates
    val eventMutableState = mutableStateOf<Event<String>?>(null)

    // Tracks the sign-in state of the user
    val signIn = mutableStateOf(false)

    // Holds the current user's data
    val userData = mutableStateOf<UserData?>(null)
    val localImagePath = mutableStateOf<String?>(null)


    // Method to update the local image path
    fun updateLocalImagePath(path: String) {
        localImagePath.value = path
    }
    // Initializes ViewModel and checks if user is already signed in
    init {
        //auth.signOut() //used for testing Authentication process
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    // Function to sign up a new user with Firebase Authentication
    fun signUp(name: String, number: String, email: String, password: String) {
        // Validate password length
        inProcess.value = true
        if (password.length < 6) {
            eventMutableState.value = Event("Password must be more than 6 characters")
            return
        }
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "some thing is empty")
            return
        }
        inProcess.value = true // Update loading state

        db.collection(USER_NODE).whereEqualTo("number", number).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) { // If number already exists
                    handleException(customMessage = "Number already exists")
                    inProcess.value = false
                    return@addOnSuccessListener // Stop further execution
                }

                // Proceed only if number does not exist
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                    } else {
                        handleException(it.exception, customMessage = "Sign-up Failed")
                    }
                    inProcess.value = false
                }
            }.addOnFailureListener {
            handleException(it, "Failed to check number existence")
            inProcess.value = false
        }


    }


    public fun signIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "please fill in all the blocks")
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() {
                if (it.isSuccessful) {

                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid.let {
                        if (it != null) {
                            getUserData(it)
                        }
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "Login Failed")
                }
            }
        }
    }

    // Function to create or update the user profile
    fun createOrUpdateProfile(name: String? = null, number: String? = null, image: String? = null) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
        uid,
        name = name ?: userData.value?.name,
        userNumber = number ?: userData.value?.userNumber,
        imageUrl = image ?: userData.value?.imageUrl
        )

        uid?.let {
            inProcess.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if (!it.exists()) {
                    db.collection(USER_NODE).document(uid).set(userData)
                }
                inProcess.value = false
                getUserData(uid)
            }.addOnFailureListener {
                handleException(it, "Cannot retrieve User")
            }
        }
    }

    // Function to retrieve user data from Firestore
    private fun getUserData(uid: String) {
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
            }
            if (error != null) {
                handleException(error, "Cannot retrieve User")
            }
        }
    }


    // Handles exceptions by logging and updating the UI state with an event message
    fun handleException(exception: Exception? = null, customMessage: String = "") {


        Log.d("TAG", "Live Chat: Exception occurred", exception) // Log exception details
        exception?.printStackTrace() // Print stack trace for debugging

        // Get the error message from exception or use a custom message
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else customMessage

        // Update the event state with the error message
        eventMutableState.value = Event(message)
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(image = it.toString())
        }
    }


    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        // Set the 'inProcess' state to true to indicate that the upload is in progress
        inProcess.value = true

        // Get a reference to the Firebase Storage
        val storageRef = storage.reference

        // Generate a unique ID (UUID) for the image file
        val uuid = UUID.randomUUID()

        // Create a reference in Firebase Storage where the image will be uploaded
        val imageRef = storageRef.child("images/$uuid")

        // Start the file upload
        val uploadTask = imageRef.putFile(uri)

        // Add an OnSuccessListener to handle a successful upload
        uploadTask.addOnSuccessListener {
            // Once the file is uploaded, retrieve the download URL
            imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                // Upload successful, set 'inProcess' to false to indicate that the task is completed
                inProcess.value = false

                // Call the onSuccess callback with the download URL of the uploaded image
                onSuccess(downloadUrl)
            }.addOnFailureListener { exception ->
                // If there's an error in retrieving the download URL, handle the exception
                handleException(exception)
                inProcess.value = false
            }
        }.addOnFailureListener { exception ->
            // If there's an error during the upload process, handle the exception
            handleException(exception)
            inProcess.value = false
        }
    }


    fun saveImageToLocalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            handleException(e, "Failed to save image locally")
            null
        }
    }


}
