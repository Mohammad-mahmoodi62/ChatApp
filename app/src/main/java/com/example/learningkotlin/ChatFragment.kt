package com.example.learningkotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.json.Json

class ChatFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        val chatRecyclerView = view.findViewById<RecyclerView>(R.id.chat_recycler_view)

        val viewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        //temp
        (activity as? MainActivity)?.test?.setChatViewModel(viewModel)

        val adapter = ChatMessageAdapter()

        chatRecyclerView.adapter = adapter

        adapter.selfId = (activity as? MainActivity)?.test?.getSelfId()!!

        viewModel.msgList.observe(this.viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it.toMutableList())
            }
        })

        val sendBtn = view.findViewById<Button>(R.id.send_button)
        val userMsg = view.findViewById<EditText>(R.id.user_message)

        sendBtn.setOnClickListener {
            val worker = Runnable {
                val chatMsg = ChatMessage(userMsg.text.toString(),
                    (activity as? MainActivity)?.test?.getSelfId().toString(),
                    (activity as? MainActivity)?.test?.otherId.toString())

                (activity as? MainActivity)?.test?.sendMsg(chatMsg)
                this.activity?.runOnUiThread { userMsg.setText("") }
            }
            threadPool.run(worker)
        }

        return view
    }

}