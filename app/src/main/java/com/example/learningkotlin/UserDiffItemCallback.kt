package com.example.learningkotlin

import androidx.recyclerview.widget.DiffUtil

class UserDiffItemCallback : DiffUtil.ItemCallback<UserInfo>() {
    override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo)
        = (oldItem.ID == newItem.ID && oldItem.Name == newItem.Name )

    override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo) = (oldItem.ID == newItem.ID)
}