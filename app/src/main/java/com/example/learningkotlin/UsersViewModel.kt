package com.example.learningkotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.FieldPosition

class UsersViewModel : ViewModel() {
    private var _userList = MutableLiveData<List<String>>()
    val userList: LiveData<List<String>> = _userList

    private val _navigateToUer = MutableLiveData<Int?>()
    val navigateToUer: MutableLiveData<Int?>
        get() = _navigateToUer

    fun addData(name:String) {
        var currentValue: MutableList<String>? = _userList.value as MutableList<String>?
        if(currentValue == null)
            currentValue = mutableListOf(name)
        else
        currentValue?.add(name)
        _userList.postValue(currentValue!!)
    }

    fun onUserClicked(position: Int) {
        _navigateToUer.value = position
    }

    fun onUserNavigated() {
        _navigateToUer.value = null
    }

}