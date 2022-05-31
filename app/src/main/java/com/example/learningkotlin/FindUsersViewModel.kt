package com.example.learningkotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindUsersViewModel: ViewModel() {
    private var _foundUserList = MutableLiveData<List<UserInfo>>()
    val foundUserList: LiveData<List<UserInfo>> = _foundUserList

    private val _onUserClicked = MutableLiveData<UserInfo?>()
    val onUserClicked: MutableLiveData<UserInfo?>
        get() = _onUserClicked


    fun addData(user:UserInfo) {
        var currentValue: MutableList<UserInfo>? = _foundUserList.value as MutableList<UserInfo>?
        if(currentValue == null)
            currentValue = mutableListOf(user)
        else
            currentValue.add(user)
        _foundUserList.postValue(currentValue!!)
    }

    fun onUserClicked(user: UserInfo) {
        _onUserClicked.value = user
    }

    fun onUserNavigated() {
        _onUserClicked.value = null
    }
}