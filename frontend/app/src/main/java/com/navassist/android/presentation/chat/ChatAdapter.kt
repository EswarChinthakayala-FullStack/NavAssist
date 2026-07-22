package com.navassist.android.presentation.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.navassist.android.databinding.ItemChatMessageBinding
import com.navassist.android.domain.model.ChatMessage

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatMessage) {
            binding.tvSenderName.text = item.senderName
            binding.tvTimestamp.text = item.timestamp.takeIf { it.isNotBlank() } ?: "Just now"

            when (item.messageType.uppercase()) {
                "IMAGE" -> {
                    binding.tvMessageText.visibility = View.GONE
                    binding.ivMessagePhoto.visibility = View.VISIBLE
                    if (!item.mediaUrl.isNullOrBlank()) {
                        binding.ivMessagePhoto.load(item.mediaUrl)
                    }
                }
                "LOCATION" -> {
                    binding.ivMessagePhoto.visibility = View.GONE
                    binding.tvMessageText.visibility = View.VISIBLE
                    binding.tvMessageText.text = "📍 ${item.messageText}"
                }
                else -> {
                    binding.ivMessagePhoto.visibility = View.GONE
                    binding.tvMessageText.visibility = View.VISIBLE
                    binding.tvMessageText.text = item.messageText
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
        }
    }
}
