package com.example.learningkotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.FieldPosition

class UsersViewModel : ViewModel() {
    private var _userList = MutableLiveData<List<UserInfo>>()
    val userList: LiveData<List<UserInfo>> = _userList

    private val _navigateToUer = MutableLiveData<String?>()
    val navigateToUer: MutableLiveData<String?>
        get() = _navigateToUer



    fun addData(user:UserInfo) {
        var currentValue: MutableList<UserInfo>? = _userList.value as MutableList<UserInfo>?
        if(currentValue == null)
            currentValue = mutableListOf(user)
        else
            currentValue.add(user)
        _userList.postValue(currentValue!!)
    }

    fun updateData(newUserID: String, lastMsg: String) {
        for(oldUser in 0 until _userList.value!!.size)
            if(_userList.value!![oldUser].ID == newUserID) {
                val currentValue: MutableList<UserInfo>? = _userList.value as MutableList<UserInfo>?
                currentValue?.set(oldUser, UserInfo(newUserID, currentValue[oldUser].Name
                                    , currentValue[oldUser].IP, lastMsg))
                _userList.postValue(currentValue!!)
            }
    }

    fun onUserClicked(userID: String) {
        _navigateToUer.value = userID
    }

    fun onUserNavigated() {
        _navigateToUer.value = null
    }

}