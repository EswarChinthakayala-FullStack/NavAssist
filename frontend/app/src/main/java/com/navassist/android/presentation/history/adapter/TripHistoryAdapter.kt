package com.navassist.android.presentation.history.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R

data class TripHistoryItem(
    val id: String,
    val bookingCode: String,
    val pickupAddress: String,
    val destinationAddress: String,
    val assistantName: String,
    val distanceKm: String,
    val fare: String,
    val dateText: String,
    val status: String = "COMPLETED"
)

class TripHistoryAdapter(
    private val onItemClick: (TripHistoryItem) -> Unit
) : ListAdapter<TripHistoryItem, TripHistoryAdapter.TripViewHolder>(TripDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip_history, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCode: TextView = itemView.findViewById(R.id.tvBookingCode)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvTripStatus)
        private val tvPickup: TextView = itemView.findViewById(R.id.tvPickupAddress)
        private val tvDrop: TextView = itemView.findViewById(R.id.tvDestinationAddress)
        private val tvFare: TextView = itemView.findViewById(R.id.tvFareText)
        private val tvAssistant: TextView = itemView.findViewById(R.id.tvAssistantInfo)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDateText)

        fun bind(item: TripHistoryItem) {
            tvCode.text = item.bookingCode
            tvStatus.text = item.status
            tvPickup.text = "● Pickup: ${item.pickupAddress}"
            tvDrop.text = "⚑ Dropoff: ${item.destinationAddress}"
            tvFare.text = item.fare
            tvAssistant.text = "Assistant: ${item.assistantName} · ${item.distanceKm}"
            tvDate.text = item.dateText
        }
    }

    private class TripDiffCallback : DiffUtil.ItemCallback<TripHistoryItem>() {
        override fun areItemsTheSame(oldItem: TripHistoryItem, newItem: TripHistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TripHistoryItem, newItem: TripHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
