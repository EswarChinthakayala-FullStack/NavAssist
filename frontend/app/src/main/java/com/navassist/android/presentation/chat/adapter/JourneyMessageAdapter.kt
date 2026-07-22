package com.navassist.android.presentation.chat.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
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
        private val container: LinearLayout = itemView.findViewById(R.id.llMessageContainer)
        private val cardBubble: MaterialCardView = itemView.findViewById(R.id.cardBubble)
        private val tvContent: TextView = itemView.findViewById(R.id.tvMessageContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimestamp)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvDeliveryStatus)

        fun bind(message: JourneyChatMessage) {
            tvContent.text = message.text
            tvTime.text = message.timestamp

            val density = itemView.context.resources.displayMetrics.density

            if (message.isFromUser) {
                container.gravity = Gravity.END
                cardBubble.setCardBackgroundColor(Color.parseColor("#27272A"))
                cardBubble.strokeColor = Color.parseColor("#3F3F46")
                cardBubble.strokeWidth = (1 * density).toInt()
                cardBubble.radius = 18f * density
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = if (message.isRead) "✓✓" else "✓"
                tvStatus.setTextColor(if (message.isRead) Color.parseColor("#22C55E") else Color.parseColor("#A1A1AA"))
            } else {
                container.gravity = Gravity.START
                cardBubble.setCardBackgroundColor(Color.parseColor("#18181B"))
                cardBubble.strokeColor = Color.parseColor("#303038")
                cardBubble.strokeWidth = (1 * density).toInt()
                cardBubble.radius = 18f * density
                tvStatus.visibility = View.GONE
            }
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
