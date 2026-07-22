package com.navassist.android.presentation.booking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.navassist.android.R
import com.navassist.android.domain.model.Assistant
import com.navassist.android.presentation.widgets.badge.DistanceEtaView

class AssistantListAdapter(
    private val onAssistantSelect: (Assistant) -> Unit
) : ListAdapter<Assistant, AssistantListAdapter.AssistantViewHolder>(AssistantDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssistantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_assistant, parent, false)
        return AssistantViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssistantViewHolder, position: Int) {
        holder.bind(getItem(position), onAssistantSelect)
    }

    class AssistantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivAvatar)
        private val tvName: TextView = itemView.findViewById(R.id.tvAssistantName)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRatingTrips)
        private val distanceEtaView: DistanceEtaView = itemView.findViewById(R.id.distanceEtaView)
        private val tvFare: TextView = itemView.findViewById(R.id.tvFareEstimate)
        private val btnSelect: MaterialButton = itemView.findViewById(R.id.btnSelectAssistant)

        fun bind(assistant: Assistant, onSelect: (Assistant) -> Unit) {
            tvName.text = assistant.name
            tvRating.text = "★ ${assistant.rating} · ${assistant.totalTrips} Trips"
            distanceEtaView.setDistanceEta(1.8, 4)
            tvFare.text = "₹${(180..320).random()}"

            if (!assistant.photoUrl.isNullOrEmpty()) {
                ivAvatar.load(assistant.photoUrl)
            } else {
                ivAvatar.setImageResource(R.drawable.ic_app_logo)
            }

            btnSelect.setOnClickListener {
                onSelect(assistant)
            }

            itemView.setOnClickListener {
                onSelect(assistant)
            }
        }
    }

    private class AssistantDiffCallback : DiffUtil.ItemCallback<Assistant>() {
        override fun areItemsTheSame(oldItem: Assistant, newItem: Assistant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Assistant, newItem: Assistant): Boolean {
            return oldItem == newItem
        }
    }
}
