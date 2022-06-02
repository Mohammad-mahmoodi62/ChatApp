package com.example.learningkotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
class ChatViewModelFactory(private val IP: String)
    : ViewModelProvider.Factory {
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(IP) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}