package com.github.aayman93.chatapp.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.repositories.MainRepository
import com.github.aayman93.chatapp.util.Event
import com.github.aayman93.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _users = MutableLiveData<Event<Resource<List<User>>>>()
    val users: LiveData<Event<Resource<List<User>>>> = _users

    init {
        getUsers()
    }

    private fun getUsers() {
        _users.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.getUsers()
            _users.postValue(Event(result))
        }
    }
}