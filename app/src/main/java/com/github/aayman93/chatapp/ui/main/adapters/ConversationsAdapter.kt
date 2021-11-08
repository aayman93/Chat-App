package com.github.aayman93.chatapp.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.github.aayman93.chatapp.data.models.Conversation
import com.github.aayman93.chatapp.databinding.ItemConversationBinding
import javax.inject.Inject

class ConversationsAdapter @Inject constructor(
    private val glide: RequestManager
) : ListAdapter<Conversation, ConversationsAdapter.ViewHolder>(ConversationDiffer) {

    private var onConversationClickListener: ((Conversation) -> Unit)? = null

    fun setOnConversationClickListener(listener: (Conversation) -> Unit) {
        onConversationClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentConversation = getItem(position)
        holder.bind(currentConversation)
    }

    companion object ConversationDiffer : DiffUtil.ItemCallback<Conversation>() {

        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.conversationId == newItem.conversationId
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            with(binding) {
                tvUsername.text = conversation.name
                tvRecentMessage.text = conversation.lastMessage
                conversation.imageUrl?.let { url ->
                    glide.load(url).into(ivProfileImage)
                }

                root.setOnClickListener {
                    onConversationClickListener?.invoke(conversation)
                }
            }
        }
    }
}