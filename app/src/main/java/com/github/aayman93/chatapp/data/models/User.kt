package com.github.aayman93.chatapp.data.models

import com.github.aayman93.chatapp.util.Constants.DEFAULT_PROFILE_PICTURE_URL

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = DEFAULT_PROFILE_PICTURE_URL
)
