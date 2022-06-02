package com.example.learningkotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatMessageAdapter() : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatDiffItemCallback()) {

    var selfId: Int = 0

    val ITEM_RECEIVED = 1
    val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):RecyclerView.ViewHolder {
        if(viewType == ITEM_RECEIVED){
            return ChatMessageAdapter.ReceivedViewHolder.inflateFrom(parent)
        }else{
            return ChatMessageAdapter.SentViewHolder.inflateFrom(parent)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if(holder.javaClass == SentViewHolder::class.java)
            (holder as SentViewHolder).bind(item)
        else
            (holder as ReceivedViewHolder).bind(item)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMsg = getItem(position)

        if(this.selfId.toString() == currentMsg.senderId)
            return ITEM_SENT
        else
            return ITEM_RECEIVED

    }

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val showMessage = itemView.findViewById<TextView>(R.id.sent_show_message)

        companion object {
            fun inflateFrom(parent: ViewGroup): ChatMessageAdapter.SentViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.chat_item_sender, parent, false)
                return ChatMessageAdapter.SentViewHolder(view)
            }
        }

        fun bind(msg: ChatMessage) {
            this.showMessage.text = msg.message
        }

    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val showMessage = itemView.findViewById<TextView>(R.id.received_show_message)

        companion object {
            fun inflateFrom(parent: ViewGroup): ChatMessageAdapter.ReceivedViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.chat_item_receiver, parent, false)
                return ChatMessageAdapter.ReceivedViewHolder(view)
            }
        }

        fun bind(msg: ChatMessage) {
            this.showMessage.text = msg.message
        }
    }

}