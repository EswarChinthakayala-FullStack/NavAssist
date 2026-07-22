package com.navassist.android.presentation.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R

data class JourneyChatMessage(
    val id: String,
    val senderId: String,
    val isFromUser: Boolean,
    val text: String,
    val timestamp: String,
    val isRead: Boolean = true
)

class JourneyMessageAdapter : ListAdapter<JourneyChatMessage, JourneyMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender: TextView? = itemView.findViewById(R.id.tvSenderName)
        private val tvContent: TextView = itemView.findViewById(R.id.tvMessageText)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(message: JourneyChatMessage) {
            tvSender?.text = if (message.isFromUser) "You" else "Assistant Guide"
            tvContent.text = message.text
            tvTime.text = message.timestamp
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<JourneyChatMessage>() {
        override fun areItemsTheSame(oldItem: JourneyChatMessage, newItem: JourneyChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JourneyChatMessage, newItem: JourneyChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
