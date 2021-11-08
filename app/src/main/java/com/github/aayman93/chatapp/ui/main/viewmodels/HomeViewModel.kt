package com.github.aayman93.chatapp.ui.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.aayman93.chatapp.data.models.Conversation
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

@ExperimentalCoroutinesApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    private val _conversations = MutableLiveData<Event<Resource<List<Conversation>>>>()
    val conversations: LiveData<Event<Resource<List<Conversation>>>> = _conversations

    private val _currentUser = MutableLiveData<Event<Resource<User>>>()
    val currentUser: LiveData<Event<Resource<User>>> = _currentUser

    init {
        getConversations()
    }

    @ExperimentalCoroutinesApi
    fun getConversations() {
        _conversations.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val startedConversationsFlow = repository.listenToStartedConversations()
                val receivedConversationsFlow = repository.listenToReceivedConversations()

                startedConversationsFlow.combine(receivedConversationsFlow) { started, received ->
                    started.onEach {
                        if (it.name == null) {
                            val user = repository.getUser(it.receiverId).data!!
                            it.name = user.username
                            it.chatReceiverId = user.uid
                            it.imageUrl = user.profilePictureUrl
                        }
                    } + received.onEach {
                        if (it.name == null) {
                            val user = repository.getUser(it.senderId).data!!
                            it.name = user.username
                            it.chatReceiverId = user.uid
                            it.imageUrl = user.profilePictureUrl
                        }
                    }
                }.collect { conversations ->
                    val sortedConversations = conversations.sortedByDescending { it.date }
                    _conversations.postValue(Event(Resource.Success(sortedConversations)))
                }

            } catch (e: Exception) {
                _conversations.postValue(
                    Event(Resource.Error(e.localizedMessage ?: "Unknown Error"))
                )
            }
        }
    }

    fun getCurrentUserDetails() {
        _currentUser.postValue(Event(Resource.Loading()))
        viewModelScope.launch(Dispatchers.Main) {
            val result = repository.getCurrentUser()
            _currentUser.postValue(Event(result))
        }
    }
}