package com.navassist.android.presentation.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.domain.model.SavedLocation
import com.navassist.android.presentation.widgets.chip.LocationChipView

class SavedLocationAdapter(
    private val onLocationClick: (SavedLocation) -> Unit
) : ListAdapter<SavedLocation, SavedLocationAdapter.SavedLocationViewHolder>(SavedLocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedLocationViewHolder {
        val chipView = LocationChipView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (10 * parent.context.resources.displayMetrics.density).toInt()
            }
        }
        return SavedLocationViewHolder(chipView)
    }

    override fun onBindViewHolder(holder: SavedLocationViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onLocationClick)
    }

    class SavedLocationViewHolder(
        private val chipView: LocationChipView
    ) : RecyclerView.ViewHolder(chipView) {

        fun bind(location: SavedLocation, onLocationClick: (SavedLocation) -> Unit) {
            chipView.setLocation(location.label, location.address)
            chipView.setOnClickListener {
                onLocationClick(location)
            }
        }
    }

    private class SavedLocationDiffCallback : DiffUtil.ItemCallback<SavedLocation>() {
        override fun areItemsTheSame(oldItem: SavedLocation, newItem: SavedLocation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedLocation, newItem: SavedLocation): Boolean {
            return oldItem == newItem
        }
    }
}
