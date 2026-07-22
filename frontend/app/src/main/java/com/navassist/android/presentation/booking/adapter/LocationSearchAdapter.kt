package com.navassist.android.presentation.booking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R
import com.navassist.android.domain.model.LocationPoint

class LocationSearchAdapter(
    private val onLocationClick: (LocationPoint) -> Unit
) : ListAdapter<LocationPoint, LocationSearchAdapter.LocationSearchViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location_search_result, parent, false)
        return LocationSearchViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationSearchViewHolder, position: Int) {
        holder.bind(getItem(position), onLocationClick)
    }

    class LocationSearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvLocationTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvLocationSubtitle)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivLocationTypeIcon)

        fun bind(item: LocationPoint, onLocationClick: (LocationPoint) -> Unit) {
            val titleText = item.name ?: item.address
            tvTitle.text = titleText
            tvSubtitle.text = item.address

            // Smart icon selector based on keywords
            val lower = titleText.lowercase()
            val iconRes = when {
                lower.contains("airport") || lower.contains("terminal") || lower.contains("flight") -> R.drawable.ic_ms_search
                lower.contains("station") || lower.contains("railway") || lower.contains("metro") -> R.drawable.ic_ms_near_me
                lower.contains("hospital") || lower.contains("clinic") || lower.contains("medical") -> R.drawable.ic_ms_warning
                lower.contains("home") -> R.drawable.ic_ms_home
                lower.contains("office") || lower.contains("work") -> R.drawable.ic_ms_layers
                else -> R.drawable.ic_ms_location_on
            }
            ivIcon.setImageResource(iconRes)

            itemView.setOnClickListener {
                onLocationClick(item)
            }
        }
    }

    private class LocationDiffCallback : DiffUtil.ItemCallback<LocationPoint>() {
        override fun areItemsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem == newItem
        }
    }
}
