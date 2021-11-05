package com.github.aayman93.chatapp.ui.main.adapters

import androidx.recyclerview.widget.DiffUtil
import com.github.aayman93.chatapp.data.models.User

class UsersDiffer : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}