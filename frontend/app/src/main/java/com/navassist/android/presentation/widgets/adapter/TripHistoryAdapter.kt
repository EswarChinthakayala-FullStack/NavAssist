package com.navassist.android.presentation.widgets.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.navassist.android.core.utils.CurrencyUtils
import com.navassist.android.databinding.ItemBookingBinding
import com.navassist.android.domain.model.Booking
import com.navassist.android.presentation.common.base.BaseListAdapter

class TripHistoryAdapter(
    private val onItemClick: (Booking) -> Unit
) : BaseListAdapter<Booking, ItemBookingBinding>(TripDiffCallback()) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemBookingBinding {
        return ItemBookingBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemBookingBinding, item: Booking, position: Int) {
        binding.tvBookingId.text = "Booking #${item.id.take(8)}"
        binding.statusBadge.setStatus(item.status.name)
        binding.tvPickup.text = "From: ${item.pickupLocation.addressName ?: "Pickup Point"}"
        binding.tvDestination.text = "To: ${item.destinationLocation.addressName ?: "Destination Point"}"
        binding.tvFare.text = CurrencyUtils.formatInr(item.fare)
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private class TripDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
    }
}
