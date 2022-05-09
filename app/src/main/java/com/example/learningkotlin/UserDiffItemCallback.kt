package com.example.learningkotlin

import androidx.recyclerview.widget.DiffUtil

class UserDiffItemCallback : DiffUtil.ItemCallback<String>() {
    override fun areContentsTheSame(oldItem: String, newItem: String)
        = (oldItem == newItem)

    override fun areItemsTheSame(oldItem: String, newItem: String) = (oldItem == newItem)
}