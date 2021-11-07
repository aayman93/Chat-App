package com.github.aayman93.chatapp.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.aayman93.chatapp.data.models.ChatMessage
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.repositories.MainRepository
import com.github.aayman93.chatapp.util.Event
import com.github.aayman93.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.Exception

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _user = MutableLiveData<Event<Resource<User>>>()
    val user: LiveData<Event<Resource<User>>> = _user

    private val _messages = MutableLiveData<Event<Resource<List<ChatMessage>>>>()
    val messages: LiveData<Event<Resource<List<ChatMessage>>>> = _messages

    private val _sendMessageStatus = MutableLiveData<Event<Resource<ChatMessage>>>()
    val sendMessageStatus: LiveData<Event<Resource<ChatMessage>>> = _sendMessageStatus

    fun getUserDetails(uid: String) {
        _user.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.getUser(uid)
            _user.postValue(Event(result))
        }
    }

    @ExperimentalCoroutinesApi
    fun getMessages(uid: String) {
        _messages.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                viewModelScope.launch {
                    val sentMessagesFlow = repository.listenToSentMessages(uid)
                    val receivedMessagesFlow = repository.listenToReceivedMessages(uid)

                    sentMessagesFlow.combine(receivedMessagesFlow) { sent, received ->
                        sent + received
                    }.collect { messages ->
                        val sortedMessages = messages.sortedBy { it.date }
                        _messages.postValue(Event(Resource.Success(sortedMessages)))
                    }
                }

            } catch (e: Exception) {
                _messages.postValue(
                    Event(Resource.Error(e.localizedMessage ?: "Unknown Error"))
                )
            }
        }
    }

    fun sendMessage(message: String, receiverUid: String) {
        _sendMessageStatus.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.sendMessage(message, receiverUid)
            _sendMessageStatus.postValue(Event(result))
        }
    }
}