package com.example.chattingapp.data

data class UserData(
    var userId: String? = "",
    var name: String? = "",
    var userNumber: String? = "",
    var imageUrl: String? = ""
) {
    fun toMap() = mapOf(
        "userId" to userId,
        "name" to name,
        "number" to userNumber,
        "imageUrl" to imageUrl


    )

}

data class ChatData(
    val chatId: String? = "",
    val user1: ChatUser = ChatUser(),
    val user2: ChatUser = ChatUser()

)

data class ChatUser(
    val userId: String? = "",
    val userName: String? = "",
    val image: String? = "",
    val number: String? = ""
)

data class Message(
    var sendBy: String? = "",
    val message: String? = "",
    val timeStamp: String? = ""
)