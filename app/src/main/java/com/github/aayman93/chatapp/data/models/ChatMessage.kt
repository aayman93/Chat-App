package com.github.aayman93.chatapp.data.models

import com.google.firebase.firestore.Exclude
import java.util.*

data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    @get:Exclude var receiverImageUrl: String? = null,
    @get:Exclude var isReceived: Boolean = false,
    val date: Date = Date(),
    val conversationId: String = ""
)
