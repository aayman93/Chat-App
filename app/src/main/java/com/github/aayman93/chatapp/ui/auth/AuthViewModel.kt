package com.github.aayman93.chatapp.ui.auth

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.aayman93.chatapp.R
import com.github.aayman93.chatapp.repositories.AuthRepository
import com.github.aayman93.chatapp.util.Constants.MAX_USERNAME_LENGTH
import com.github.aayman93.chatapp.util.Constants.MIN_PASSWORD_LENGTH
import com.github.aayman93.chatapp.util.Constants.MIN_USERNAME_LENGTH
import com.github.aayman93.chatapp.util.Event
import com.github.aayman93.chatapp.util.Resource
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    @ApplicationContext private val applicationContext: Context
) : ViewModel() {

    private val _registerStatus = MutableLiveData<Event<Resource<AuthResult>>>()
    val registerStatus: LiveData<Event<Resource<AuthResult>>> = _registerStatus

    private val _loginStatus = MutableLiveData<Event<Resource<AuthResult>>>()
    val loginStatus: LiveData<Event<Resource<AuthResult>>> = _loginStatus

    fun register(username: String, email: String, password: String, confirmedPassword: String) {

        val error = if (username.isBlank() || email.isBlank() || password.isBlank()
            || confirmedPassword.isBlank()
        ) {
            applicationContext.getString(R.string.error_empty_fields)
        } else if (username.length < MIN_USERNAME_LENGTH) {
            applicationContext.getString(R.string.error_username_too_short, MIN_USERNAME_LENGTH)
        }
        else if (username.length > MAX_USERNAME_LENGTH) {
            applicationContext.getString(R.string.error_username_too_long, MAX_USERNAME_LENGTH)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            applicationContext.getString(R.string.error_invalid_email)
        } else if (password.length < MIN_PASSWORD_LENGTH) {
            applicationContext.getString(R.string.error_password_too_short, MIN_PASSWORD_LENGTH)
        } else if (password != confirmedPassword) {
            applicationContext.getString(R.string.error_password_mismatch)
        }  else null

        error?.let {
            _registerStatus.postValue(Event(Resource.Error(it)))
            return
        }

        _registerStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.register(username, email, password)
            _registerStatus.postValue(Event(result))
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            val error = applicationContext.getString(R.string.error_empty_fields)
            _loginStatus.postValue(Event(Resource.Error(error)))
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            val error = applicationContext.getString(R.string.error_invalid_email)
            _loginStatus.postValue(Event(Resource.Error(error)))
        } else {
            _loginStatus.postValue(Event(Resource.Loading()))
            viewModelScope.launch(Dispatchers.Main) {
                val result = repository.login(email, password)
                _loginStatus.postValue(Event(result))
            }
        }
    }

}