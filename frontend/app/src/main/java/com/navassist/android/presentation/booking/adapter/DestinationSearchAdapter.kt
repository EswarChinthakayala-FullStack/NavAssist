package com.navassist.android.presentation.booking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R
import com.navassist.android.domain.model.LocationPoint

class DestinationSearchAdapter(
    private val onDestinationClick: (LocationPoint) -> Unit
) : ListAdapter<LocationPoint, DestinationSearchAdapter.DestinationViewHolder>(DestinationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location_search_result, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(getItem(position), onDestinationClick)
    }

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvLocationTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvLocationSubtitle)

        fun bind(item: LocationPoint, onDestinationClick: (LocationPoint) -> Unit) {
            tvTitle.text = item.name ?: item.address
            tvSubtitle.text = item.address
            itemView.setOnClickListener {
                onDestinationClick(item)
            }
        }
    }

    private class DestinationDiffCallback : DiffUtil.ItemCallback<LocationPoint>() {
        override fun areItemsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem == newItem
        }
    }
}
