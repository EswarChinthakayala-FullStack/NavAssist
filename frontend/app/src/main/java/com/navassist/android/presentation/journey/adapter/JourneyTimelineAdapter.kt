package com.navassist.android.presentation.journey.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R

data class TimelineItem(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val isCompleted: Boolean = true
)

class JourneyTimelineAdapter : ListAdapter<TimelineItem, JourneyTimelineAdapter.TimelineViewHolder>(TimelineDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_journey_timeline, parent, false)
        return TimelineViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBullet: TextView = itemView.findViewById(R.id.tvTimelineBullet)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTimelineTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvTimelineDesc)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTimelineTime)

        fun bind(item: TimelineItem) {
            tvTitle.text = item.title
            tvDesc.text = item.description
            tvTime.text = item.time

            if (item.isCompleted) {
                tvBullet.text = "✔"
                tvBullet.setTextColor(Color.parseColor("#22C55E"))
            } else {
                tvBullet.text = "●"
                tvBullet.setTextColor(Color.parseColor("#71717A"))
            }
        }
    }

    private class TimelineDiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean {
            return oldItem == newItem
        }
    }
}
