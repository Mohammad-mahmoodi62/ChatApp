package com.example.learningkotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.FieldPosition

class ChatViewModel : ViewModel() {
    private var _msgList = MutableLiveData<List<ChatMessage>>()
    val msgList: LiveData<List<ChatMessage>> = _msgList

    fun addData(msg: ChatMessage) {
        var currentValue: MutableList<ChatMessage>? = _msgList.value as MutableList<ChatMessage>?
        if(currentValue == null)
            currentValue = mutableListOf(msg)
        else
            currentValue?.add(msg)
        _msgList.postValue(currentValue!!)
    }

}