package com.example.learningkotlin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ChatUserAdapter(val clickListener: (UserID: String) -> Unit) : ListAdapter<UserInfo, ChatUserAdapter.FoundUserViewHolder>(UserDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            FoundUserViewHolder = FoundUserViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: FoundUserViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, this.clickListener)
    }

    class FoundUserViewHolder(val rootView: CardView)
        : RecyclerView.ViewHolder(rootView) {
        private val userName: TextView = itemView.findViewById(R.id.chat_user_name)
        private val lastMsg: TextView = itemView.findViewById(R.id.last_message)

            companion object {
                fun inflateFrom(parent: ViewGroup): FoundUserViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val view = layoutInflater
                        .inflate(R.layout.user_item, parent, false) as CardView
                    return FoundUserViewHolder(view)
                }
            }

        fun bind(user: UserInfo, clickListener: (UserID: String) -> Unit) {
            this.userName.text  = user.Name
            this.lastMsg.text = user.lastMsg
            rootView.setOnClickListener {
                clickListener(user.ID)
            }
        }

        }
}