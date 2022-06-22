package com.example.learningkotlin

import androidx.recyclerview.widget.DiffUtil

class UserDiffItemCallback : DiffUtil.ItemCallback<UserInfo>() {
    override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo) : Boolean
    {
        return (oldItem.lastMsg == newItem.lastMsg )
    }

    override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo) : Boolean {
        return (oldItem.ID == newItem.ID)
    }
}