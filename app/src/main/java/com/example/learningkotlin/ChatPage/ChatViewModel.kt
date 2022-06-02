package com.example.learningkotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.FieldPosition

class ChatViewModel(IP: String) : ViewModel() {
    private var _msgList = ConnectionHandler.getMsgList(IP)
    val msgList: LiveData<List<ChatMessage>>? = _msgList

}