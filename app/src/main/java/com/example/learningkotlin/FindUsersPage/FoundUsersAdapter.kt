package com.example.learningkotlin

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class FoundUsersAdapter(val clickListener: (user: UserInfo) -> Unit) : ListAdapter<UserInfo, FoundUsersAdapter.FoundUserViewHolder>(UserDiffItemCallback()){

    class FoundUserViewHolder(val rootView: CardView) : RecyclerView.ViewHolder(rootView) {
        private val userName: TextView = itemView.findViewById(R.id.found_user_name)
        companion object {
            fun inflateFrom(parent: ViewGroup) : FoundUserViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.found_user, parent, false) as CardView
                return FoundUsersAdapter.FoundUserViewHolder(view)
            }
        }

        fun bind(user: UserInfo, clickListener: (user: UserInfo) -> Unit) {
            this.userName.text = user.Name
            rootView.setOnClickListener {
                clickListener(user)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoundUserViewHolder {
        return FoundUsersAdapter.FoundUserViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: FoundUserViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, this.clickListener)
    }
}