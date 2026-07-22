package com.navassist.android.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.navassist.android.R
import com.navassist.android.domain.model.ChatMessage

class ChatMessageAdapter : ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessageText)
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivMessagePhoto)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(item: ChatMessage) {
            tvSender.text = item.senderName
            tvTime.text = item.timestamp.takeIf { it.isNotBlank() } ?: "Just now"

            when (item.messageType.uppercase()) {
                "IMAGE" -> {
                    tvMessage.visibility = View.GONE
                    ivPhoto.visibility = View.VISIBLE
                    if (!item.mediaUrl.isNullOrBlank()) {
                        ivPhoto.load(item.mediaUrl)
                    }
                }
                "LOCATION" -> {
                    ivPhoto.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = "📍 ${item.messageText}"
                }
                else -> {
                    ivPhoto.visibility = View.GONE
                    tvMessage.visibility = View.VISIBLE
                    tvMessage.text = item.messageText
                }
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
    }
}
