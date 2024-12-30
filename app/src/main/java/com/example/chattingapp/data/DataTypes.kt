package com.example.chattingapp.data

data class UserData(
    var userId:String?="",
    var name :String?="",
    var userNumber:String?="",
    var imageUrl:String?=""){
    fun toMap()= mapOf(
        "userId" to userId,
        "name" to name,
        "number" to userNumber,
        "imageUrl" to imageUrl


    )
}
