package com.example.chattingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.chattingapp.data.CHATS
import com.example.chattingapp.data.ChatData
import com.example.chattingapp.data.ChatUser
import com.example.chattingapp.data.Event
import com.example.chattingapp.data.ToastUtil
import com.example.chattingapp.data.USER_NODE
import com.example.chattingapp.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileReader
import java.util.UUID
import javax.inject.Inject

// ViewModel for managing user authentication and related state
@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    // Mutable state variables for UI updates and data tracking
    var inProcess = mutableStateOf(false) // Tracks loading state
    var inProcessChat = mutableStateOf(false) // Tracks chat-related processes
    val chats = mutableStateOf<List<ChatData>>(emptyList()) // List of chat data
    val eventMutableState = mutableStateOf<Event<String>?>(null) // Tracks events like errors or success messages
    val signIn = mutableStateOf(false) // Tracks user sign-in status
    val userData = mutableStateOf<UserData?>(null) // Stores user profile data
    val localImagePath = mutableStateOf<String?>(null) // Stores local image file path

    /**
     * Updates the locally stored image path.
     * @param path The file path of the local image.
     */
    fun updateLocalImagePath(path: String) {
        localImagePath.value = path
    }

    /**
     * Initializes ViewModel by checking user authentication status.
     * Retrieves user data if signed in.
     */
    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    /**
     * Registers a new user.
     * @param name User's name.
     * @param number User's phone number.
     * @param email User's email.
     * @param password User's password.
     */
    fun signUp(name: String, number: String, email: String, password: String) {
        inProcess.value = true

        // Validate input fields
        if (password.length < 6) {
            eventMutableState.value = Event("Password must be more than 6 characters")
            return
        }
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Some fields are empty")
            return
        }

        // Check if the number already exists
        db.collection(USER_NODE).whereEqualTo("number", number).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    handleException(customMessage = "Number already exists")
                    inProcess.value = false
                    return@addOnSuccessListener
                }

                // Register user with Firebase Authentication
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

    /**
     * Authenticates existing user.
     * @param email User's email.
     * @param password User's password.
     */
    fun signIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
        } else {
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                } else {
                    handleException(exception = it.exception, customMessage = "Login Failed")
                }
            }
        }
    }

    /**
     * Creates or updates user profile.
     * @param name User's name.
     * @param number User's phone number.
     * @param image Optional image URL.
     */
    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        image: String? = null
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            handleException(customMessage = "User not authenticated")
            return
        }

        if (name.isNullOrEmpty() || number.isNullOrEmpty()) {
            handleException(customMessage = "Name and Number cannot be empty")
            return
        }

        inProcess.value = true

        val updatedUserData = UserData(
            userId = uid,
            name = name,
            userNumber = number,
            imageUrl = image ?: userData.value?.imageUrl
        )

        val userDocument = db.collection(USER_NODE).document(uid)

        userDocument.get()
            .addOnSuccessListener { documentSnapshot ->
                val task = if (documentSnapshot.exists()) {
                    userDocument.update(
                        mapOf(
                            "name" to name,
                            "userNumber" to number,
                            "imageUrl" to updatedUserData.imageUrl
                        )
                    )
                } else {
                    userDocument.set(updatedUserData)
                }

                task.addOnSuccessListener {
                    getUserData(uid)
                    inProcess.value = false
                    eventMutableState.value = Event(
                        if (documentSnapshot.exists()) "Profile updated successfully" else "Profile created successfully"
                    )
                }.addOnFailureListener {
                    handleException(it, "Failed to save profile")
                    inProcess.value = false
                }
            }
            .addOnFailureListener {
                handleException(it, "Error checking profile existence")
                inProcess.value = false
            }
    }

    /**
     * Logs out the user and resets state.
     */
    fun signOut() {
        auth.signOut()
        signIn.value = false
    }

    // Retrieve User Data
    /**
     * Retrieves user data from Firestore based on the provided UID and updates the `userData` state.
     * It also populates the chat list after successful retrieval.
     *
     * @param uid The unique identifier of the user to fetch data for.
     */
    private fun getUserData(uid: String) {
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                populateChats() // Refresh the chat list after user data retrieval.
            }
            if (error != null) {
                handleException(error, "Cannot retrieve User")
            }
        }
    }

    /**
     * Saves an image locally to the app's internal storage and returns the absolute file path.
     *
     * @param uri The URI of the image to be saved.
     * @param context The context used to access content resolver and file system.
     * @return The absolute path of the saved image file.
     */
    private fun saveImageLocally(uri: Uri, context: Context): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    /**
     * Saves the profile image by updating the local image path.
     * This function is currently a placeholder for further image processing.
     *
     * @param uri The URI of the profile image.
     * @param context The application context for handling the file.
     */
    fun saveProfileImage(uri: Uri, context: Context) {
        // Save the image URI to update the UI.
        updateLocalImagePath(uri.toString())
    }

    /**
     * Handles exceptions by logging the error, showing a toast message, and updating the event state.
     *
     * @param exception The exception object, if available (nullable).
     * @param customMessage A custom message to display if provided.
     */
    fun handleException(exception: Exception? = null, customMessage: String = "") {
        Log.d("TAG", "Live Chat: Exception occurred", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty()) errorMsg else customMessage
        eventMutableState.value = Event(message)
        ToastUtil.showToast(customMessage)
    }

    /**
     * Initiates the creation of a new chat by validating the provided number and checking existing chats.
     *
     * @param number The phone number of the user to start a chat with.
     */
    fun onAddChat(number: String) {
        if (number.isBlank() || !number.isDigitsOnly()) {
            handleException(customMessage = "Invalid number. Please enter digits only.")
            return
        }

        val currentUser = userData.value ?: run {
            handleException(customMessage = "User data is unavailable")
            return
        }

        // Check if a chat already exists between the users.
        db.collection(CHATS)
            .where(
                Filter.or(
                    Filter.and(
                        Filter.equalTo("user1.userNumber", number),
                        Filter.equalTo("user2.userNumber", currentUser.userNumber)
                    ),
                    Filter.and(
                        Filter.equalTo("user2.userNumber", number),
                        Filter.equalTo("user1.userNumber", currentUser.userNumber)
                    )
                )
            )
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    handleException(customMessage = "Chat already exists")
                    return@addOnSuccessListener
                }

                // Find the user associated with the provided number.
                db.collection(USER_NODE)
                    .whereEqualTo("userNumber", number)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        if (userSnapshot.isEmpty) {
                            handleException(customMessage = "User not found")
                            return@addOnSuccessListener
                        }

                        val chatPartner = userSnapshot.toObjects<UserData>().first()
                        val chatId = db.collection(CHATS).document().id
                        val chatData = ChatData(
                            chatId = chatId,
                            user1 = ChatUser(
                                userId = currentUser.userId,
                                userName = currentUser.name,
                                image = currentUser.imageUrl,
                                number = currentUser.userNumber
                            ),
                            user2 = ChatUser(
                                userId = chatPartner.userId,
                                userName = chatPartner.name,
                                image = chatPartner.imageUrl,
                                number = chatPartner.userNumber
                            )
                        )

                        // Create the new chat document in Firestore.
                        db.collection(CHATS).document(chatId).set(chatData)
                            .addOnSuccessListener {
                                eventMutableState.value = Event("Chat added successfully")
                                populateChats() // Refresh the chat list after adding.
                            }
                            .addOnFailureListener {
                                handleException(it, "Failed to create chat")
                            }
                    }
                    .addOnFailureListener {
                        handleException(it, "Failed to check user existence")
                    }
            }
            .addOnFailureListener {
                handleException(it, "Failed to check existing chats")
            }
    }

    /**
     * Fetches and populates the list of chats involving the current user.
     */
    private fun populateChats() {
        val currentUser = userData.value ?: return

        db.collection(CHATS)
            .where(
                Filter.or(
                    Filter.equalTo("user1.userId", currentUser.userId),
                    Filter.equalTo("user2.userId", currentUser.userId)
                )
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleException(error, "Failed to fetch chats")
                    return@addSnapshotListener
                }

                val chatList = snapshot?.toObjects<ChatData>()?.map { chat ->
                    // Determine the other user in the chat.
                    val otherUser = if (chat.user1.userId == currentUser.userId) {
                        chat.user2 // Show user2 if current user is user1.
                    } else {
                        chat.user1 // Show user1 if current user is user2.
                    }

                    // Create the ChatData object, passing relevant properties.
                    ChatData(
                        chatId = chat.chatId,
                        user1 = chat.user1,
                        user2 = chat.user2,
                    )
                } ?: emptyList()

                chats.value = chatList
            }
    }




    //TODO
    fun getImageFromLocalStorage(path: String): Bitmap? {
        return try {
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /* fun saveProfileImage(uri: Uri, context: Context) {
         val uid = auth.currentUser?.uid
         if (uid == null) {
             handleException(customMessage = "User not authenticated")
             return
         }

         // Create a reference for the image file in Firebase Storage
         val storageRef = storage.reference.child("profile_images/$uid/${UUID.randomUUID()}")

         // Upload the image to Firebase Storage
         val uploadTask = storageRef.putFile(uri)
         uploadTask.addOnSuccessListener {
             // Get the download URL of the uploaded image
             storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                 // Save the image URL to Firestore
                 createOrUpdateProfile(image = downloadUri.toString())

                 // Save the local image path
                 val localImagePath = saveImageLocally(uri, context)
                 updateLocalImagePath(localImagePath)
             }
         }.addOnFailureListener {
             handleException(it, "Failed to upload image")
         }
     }*/

}
