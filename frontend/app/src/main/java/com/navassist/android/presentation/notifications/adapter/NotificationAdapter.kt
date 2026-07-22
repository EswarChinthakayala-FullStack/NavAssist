package com.navassist.android.presentation.notifications.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val category: String = "BOOKING"
)

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationItem, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardNotification)
        private val tvUnread: TextView = itemView.findViewById(R.id.tvUnreadIndicator)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvNotifCategory)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotifTime)
        private val tvBody: TextView = itemView.findViewById(R.id.tvNotifBody)

        fun bind(item: NotificationItem) {
            tvCategory.text = item.title
            tvBody.text = item.body
            tvTime.text = item.timestamp

            if (item.isRead) {
                tvUnread.visibility = View.GONE
                card.setCardBackgroundColor(Color.parseColor("#18181B"))
                tvCategory.setTextColor(Color.parseColor("#A1A1AA"))
            } else {
                tvUnread.visibility = View.VISIBLE
                card.setCardBackgroundColor(Color.parseColor("#242428"))
                tvCategory.setTextColor(Color.parseColor("#FAFAFA"))
            }
        }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}
