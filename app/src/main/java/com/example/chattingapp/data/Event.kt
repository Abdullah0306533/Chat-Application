package com.example.chattingapp.data

open class Event<out T>(val content: T) {
    var hasBeenHandled = false

    fun consumeContent(): T? { // Renamed to avoid conflict
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
}
