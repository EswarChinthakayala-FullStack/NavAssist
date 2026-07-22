package com.navassist.android.presentation.widgets.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.navassist.android.databinding.ViewAssistantCardBinding
import com.navassist.android.domain.model.Assistant
import com.navassist.android.presentation.common.base.BaseListAdapter

class AssistantListAdapter(
    private val onItemClick: (Assistant) -> Unit
) : BaseListAdapter<Assistant, ViewAssistantCardBinding>(AssistantDiffCallback()) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ViewAssistantCardBinding {
        return ViewAssistantCardBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ViewAssistantCardBinding, item: Assistant, position: Int) {
        binding.tvName.text = item.name
        binding.tvRating.text = "★ ${String.format("%.1f", item.rating)}"
        binding.tvVehicle.text = item.vehicleDetails ?: "Standard Assistant Vehicle"
        binding.tvTrips.text = "${item.totalTrips} completed trips"
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private class AssistantDiffCallback : DiffUtil.ItemCallback<Assistant>() {
        override fun areItemsTheSame(oldItem: Assistant, newItem: Assistant): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Assistant, newItem: Assistant): Boolean = oldItem == newItem
    }
}
