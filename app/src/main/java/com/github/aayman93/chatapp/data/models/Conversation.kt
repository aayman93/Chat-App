package com.github.aayman93.chatapp.data.models

import com.google.firebase.firestore.Exclude
import java.util.*

data class Conversation(
    val conversationId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val lastMessage: String = "",
    val date: Date = Date(),
    @get:Exclude var name: String? = null,
    @get:Exclude var imageUrl: String? = null,
    @get:Exclude var chatReceiverId: String? = null
)
