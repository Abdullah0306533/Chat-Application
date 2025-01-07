package com.example.chattingapp.data

import android.content.Context
import android.widget.Toast

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
object ToastUtil {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext // Store application context
    }

    fun showToast(message: String) {
        appContext?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }
}
object GetContext{
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context // Store application context
    }
    fun getContext():Context?= appContext

}