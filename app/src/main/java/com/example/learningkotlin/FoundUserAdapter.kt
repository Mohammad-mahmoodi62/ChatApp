package com.example.learningkotlin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class FoundUserAdapter(val clickListener: (UserID: String) -> Unit) : ListAdapter<UserInfo, FoundUserAdapter.FoundUserViewHolder>(UserDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            FoundUserViewHolder = FoundUserViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: FoundUserViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, this.clickListener)
    }

    class FoundUserViewHolder(val rootView: TextView)
        : RecyclerView.ViewHolder(rootView) {

            companion object {
                fun inflateFrom(parent: ViewGroup): FoundUserViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val view = layoutInflater
                        .inflate(R.layout.found_user, parent, false) as TextView
                    return FoundUserViewHolder(view)
                }
            }

        fun bind(user: UserInfo, clickListener: (UserID: String) -> Unit) {
            rootView.text = user.Name
            rootView.setOnClickListener {
                clickListener(user.ID)
            }
        }

        }
}