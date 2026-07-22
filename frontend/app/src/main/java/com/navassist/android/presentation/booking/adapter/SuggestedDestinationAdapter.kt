package com.navassist.android.presentation.booking.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.presentation.widgets.chip.LocationChipView

class SuggestedDestinationAdapter(
    private val onSuggestedClick: (LocationPoint) -> Unit
) : ListAdapter<LocationPoint, SuggestedDestinationAdapter.SuggestedViewHolder>(SuggestedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestedViewHolder {
        val chipView = LocationChipView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (10 * parent.context.resources.displayMetrics.density).toInt()
            }
        }
        return SuggestedViewHolder(chipView)
    }

    override fun onBindViewHolder(holder: SuggestedViewHolder, position: Int) {
        holder.bind(getItem(position), onSuggestedClick)
    }

    class SuggestedViewHolder(private val chipView: LocationChipView) : RecyclerView.ViewHolder(chipView) {
        fun bind(point: LocationPoint, onSuggestedClick: (LocationPoint) -> Unit) {
            chipView.setLocation(point.name ?: "Suggested", point.address)
            chipView.setOnClickListener {
                onSuggestedClick(point)
            }
        }
    }

    private class SuggestedDiffCallback : DiffUtil.ItemCallback<LocationPoint>() {
        override fun areItemsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem == newItem
        }
    }
}
