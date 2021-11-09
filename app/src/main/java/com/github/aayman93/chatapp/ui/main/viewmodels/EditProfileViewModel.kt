package com.github.aayman93.chatapp.ui.main.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.aayman93.chatapp.R
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.repositories.MainRepository
import com.github.aayman93.chatapp.util.Constants.MAX_USERNAME_LENGTH
import com.github.aayman93.chatapp.util.Constants.MIN_USERNAME_LENGTH
import com.github.aayman93.chatapp.util.Event
import com.github.aayman93.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: MainRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _currentUser = MutableLiveData<Event<Resource<User>>>()
    val currentUser: LiveData<Event<Resource<User>>> = _currentUser
    
    private val _updateProfileStatus = MutableLiveData<Event<Resource<Any>>>()
    val updateProfileStatus: LiveData<Event<Resource<Any>>> = _updateProfileStatus

    private val _selectedImageUri = MutableLiveData<Uri>()
    val selectedImageUri: LiveData<Uri> = _selectedImageUri

    var isFirstLoad: Boolean = true

    var hasBeenHandled: Boolean = false

    init {
        loadUserDetails()
    }

    private fun loadUserDetails() {
        _currentUser.postValue(Event(Resource.Loading()))
        getUser()
    }

    fun setSelectedImageUri(uri: Uri) {
        _selectedImageUri.postValue(uri)
    }

    fun updateProfile(currentUser: User, username: String, imageUri: Uri?) {
        val error = if (username == currentUser.username && imageUri == null) {
            applicationContext.getString(R.string.error_no_changes)
        } else if (username.isBlank()) {
            applicationContext.getString(R.string.error_username_empty)
        } else if (username.length < MIN_USERNAME_LENGTH) {
            applicationContext.getString(R.string.error_username_too_short)
        } else if (username.length > MAX_USERNAME_LENGTH) {
            applicationContext.getString(R.string.error_username_too_long)
        } else {
            null
        }

        error?.let {
            _updateProfileStatus.postValue(Event(Resource.Error(it)))
            return
        }

        _updateProfileStatus.postValue(Event(Resource.Loading()))
        hasBeenHandled = false
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.updateProfile(currentUser, username, imageUri)
            _updateProfileStatus.postValue(Event(result))
        }
    }

    fun getUser() {
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.getCurrentUser()
            _currentUser.postValue(Event(result))
        }
    }
}