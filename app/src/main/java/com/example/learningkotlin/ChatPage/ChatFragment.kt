package com.example.learningkotlin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
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

        val userID = ChatFragmentArgs.fromBundle(requireArguments()).userID
        val currentUser = ConnectionHandler.getUser(userID)

        this.updateToolBar(currentUser!!)

        val viewModelFactory = ChatViewModelFactory(currentUser!!.IP)
        val viewModel = ViewModelProvider(
            this, viewModelFactory).get(ChatViewModel::class.java)

        val adapter = ChatMessageAdapter()

        chatRecyclerView.adapter = adapter

        //TODO: add selfID
        adapter.selfId = ConnectionHandler.selfID!!

        viewModel.msgList!!.observe(this.viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it.toMutableList())
                chatRecyclerView.post {
                    Thread.sleep(4)
                    chatRecyclerView.layoutManager?.scrollToPosition(it.size - 1)
                }
            }
        })

        val sendBtn = view.findViewById<ImageView>(R.id.send_button)
        val userMsg = view.findViewById<EditText>(R.id.user_message)

        userMsg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                return
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, count: Int) {
                if(p0?.length!! > 0) {
                    val newColor = context?.resources?.getColor(R.color.main_color)
                    sendBtn.setColorFilter(newColor!!)
                }
                else if (p0.isEmpty()) {
                    val newColor = context?.resources?.getColor(R.color.secondary_color)
                    sendBtn.setColorFilter(newColor!!)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                return
            }

        })

        sendBtn.setOnClickListener {
            val worker = Runnable {
                val chatMsg = ChatMessage(userMsg.text.toString(), adapter.selfId, currentUser!!.ID)

                val result = ConnectionHandler.sendMsg(chatMsg, currentUser!!.IP)
                this.activity?.runOnUiThread {
                    userMsg.setText("")
                    if(!result!!)
                        Toast.makeText(this.context, "${currentUser.Name} is disconnected "
                        + "cannot send Message", Toast.LENGTH_LONG).show()
                    else
                        ConnectionHandler.addChatMsgToMap(currentUser!!.IP,chatMsg)

                }
            }
            threadPool.run(worker)
        }

        return view
    }

    private fun updateToolBar(currentUser: UserInfo) {
        (activity as MainActivity).toolbarText.text = currentUser.Name
        //TODO add Image as well
        (activity as MainActivity).toolbarImage.visibility = View.VISIBLE
        (activity as MainActivity).supportActionBar?.title = ""
    }

}