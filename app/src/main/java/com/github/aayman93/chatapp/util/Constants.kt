package com.github.aayman93.chatapp.util

object Constants {
    const val DEFAULT_PROFILE_PICTURE_URL = "https://firebasestorage.googleapis.com/v0/b/chat-app-40776.appspot.com/o/default_profile_picture.jpg?alt=media&token=a84486bc-4d34-4ff6-818b-25236f1561ac"

    const val MIN_USERNAME_LENGTH = 4
    const val MAX_USERNAME_LENGTH = 20
    const val MIN_PASSWORD_LENGTH = 8

    const val KEY_COLLECTION_USERS = "users"
    const val KEY_USER_PROFILE_PICTURE = "profilePictureUrl"
    const val KEY_USER_USERNAME = "username"

    const val KEY_COLLECTION_CHAT = "chat"
    const val KEY_SENDER_ID = "senderId"
    const val KEY_RECEIVER_ID = "receiverId"

    const val KEY_COLLECTION_CONVERSATIONS = "conversations"
    const val KEY_CONVERSATION_LAST_MESSAGE = "lastMessage"
    const val KEY_CONVERSATION_DATE = "date"

    const val VIEW_TYPE_MESSAGE_SENT = 1
    const val VIEW_TYPE_MESSAGE_RECEIVED = 2
}