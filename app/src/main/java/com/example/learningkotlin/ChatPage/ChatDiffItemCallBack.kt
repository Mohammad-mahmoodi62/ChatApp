package com.example.learningkotlin

import androidx.recyclerview.widget.DiffUtil

class ChatDiffItemCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) :Boolean {
        return (oldItem.message == newItem.message)
    }

    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) :Boolean {
            return (oldItem.toString() == newItem.toString())
    }
}