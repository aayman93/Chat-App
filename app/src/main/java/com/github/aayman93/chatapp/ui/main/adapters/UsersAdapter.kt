package com.github.aayman93.chatapp.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.github.aayman93.chatapp.data.models.User
import com.github.aayman93.chatapp.databinding.ItemUserBinding
import javax.inject.Inject

class UsersAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<User, UsersAdapter.ViewHolder>(UsersDiffer()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentUser = getItem(position)
        holder.bind(currentUser)
    }

    inner class ViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            with(binding) {
                tvUsername.text = user.username
                tvEmail.text = user.email
                glide.load(user.profilePictureUrl).into(ivProfileImage)
            }
        }
    }
}