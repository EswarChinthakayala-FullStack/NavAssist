package com.navassist.android.presentation.booking.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.presentation.widgets.chip.LocationChipView

class ServicePointChipAdapter(
    private val onPointClick: (LocationPoint) -> Unit
) : ListAdapter<LocationPoint, ServicePointChipAdapter.ServicePointViewHolder>(PointDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicePointViewHolder {
        val chipView = LocationChipView(parent.context).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (10 * parent.context.resources.displayMetrics.density).toInt()
            }
        }
        return ServicePointViewHolder(chipView)
    }

    override fun onBindViewHolder(holder: ServicePointViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onPointClick)
    }

    class ServicePointViewHolder(private val chipView: LocationChipView) : RecyclerView.ViewHolder(chipView) {
        fun bind(point: LocationPoint, onPointClick: (LocationPoint) -> Unit) {
            chipView.setLocation(point.name ?: "Service Hub", point.address)
            chipView.setOnClickListener {
                onPointClick(point)
            }
        }
    }

    private class PointDiffCallback : DiffUtil.ItemCallback<LocationPoint>() {
        override fun areItemsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: LocationPoint, newItem: LocationPoint): Boolean {
            return oldItem == newItem
        }
    }
}
