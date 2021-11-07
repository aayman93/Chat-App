package com.github.aayman93.chatapp.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.github.aayman93.chatapp.data.models.ChatMessage
import com.github.aayman93.chatapp.databinding.ItemReceivedMessageBinding
import com.github.aayman93.chatapp.databinding.ItemSentMessageBinding
import com.github.aayman93.chatapp.util.Constants.VIEW_TYPE_MESSAGE_RECEIVED
import com.github.aayman93.chatapp.util.Constants.VIEW_TYPE_MESSAGE_SENT
import com.github.aayman93.chatapp.util.getReadableDateTime
import javax.inject.Inject

class ChatAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            val binding = ItemReceivedMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ReceivedMessageViewHolder(binding)
        } else {
            val binding = ItemSentMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SentMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = getItem(position)
        when (getItemViewType(position)) {
            VIEW_TYPE_MESSAGE_RECEIVED -> (holder as ReceivedMessageViewHolder).bind(currentMessage)
            else -> (holder as SentMessageViewHolder).bind(currentMessage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = getItem(position)
        return if (currentMessage.isReceived) {
            VIEW_TYPE_MESSAGE_RECEIVED
        } else {
            VIEW_TYPE_MESSAGE_SENT
        }
    }

    companion object Differ : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }

    inner class ReceivedMessageViewHolder(
        private val binding: ItemReceivedMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            with(binding) {
                tvMessageContent.text = chatMessage.message
                tvTimestamp.text = chatMessage.date.getReadableDateTime()
                chatMessage.receiverImageUrl?.let { url ->
                    glide.load(url).into(ivProfileImage)
                }
            }
        }
    }

    inner class SentMessageViewHolder(
        private val binding: ItemSentMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            with(binding) {
                tvMessageContent.text = chatMessage.message
                tvTimestamp.text = chatMessage.date.getReadableDateTime()
            }
        }
    }
}