package com.navassist.android.presentation.profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R

data class SavedLocationItem(
    val id: String,
    val label: String,
    val address: String,
    val landmark: String,
    val iconSymbol: String = "📍",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

class SavedLocationAdapter(
    private val onItemClick: (SavedLocationItem) -> Unit
) : ListAdapter<SavedLocationItem, SavedLocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvLocationIcon)
        private val tvLabel: TextView = itemView.findViewById(R.id.tvLocationLabel)
        private val tvAddress: TextView = itemView.findViewById(R.id.tvLocationAddress)
        private val tvLandmark: TextView = itemView.findViewById(R.id.tvLocationLandmark)

        fun bind(item: SavedLocationItem) {
            tvIcon.text = item.iconSymbol
            tvLabel.text = item.label
            tvAddress.text = item.address
            tvLandmark.text = item.landmark
        }
    }

    private class LocationDiffCallback : DiffUtil.ItemCallback<SavedLocationItem>() {
        override fun areItemsTheSame(oldItem: SavedLocationItem, newItem: SavedLocationItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedLocationItem, newItem: SavedLocationItem): Boolean {
            return oldItem == newItem
        }
    }
}
