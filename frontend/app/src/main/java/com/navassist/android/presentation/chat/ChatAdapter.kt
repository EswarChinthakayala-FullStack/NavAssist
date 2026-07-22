package com.navassist.android.presentation.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R
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
            binding.tvMessageContent.text = item.messageText
            binding.tvTimestamp.text = item.timestamp

            val card = binding.cardBubble
            val params = card.layoutParams as ViewGroup.MarginLayoutParams
            if (item.isFromMe) {
                card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.primary_accent))
                binding.tvMessageContent.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_on_primary))
                params.setMargins(100, 0, 0, 0)
            } else {
                card.setCardBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.surface_variant))
                binding.tvMessageContent.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_primary))
                params.setMargins(0, 0, 100, 0)
            }
            card.layoutParams = params
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
        }
    }
}
