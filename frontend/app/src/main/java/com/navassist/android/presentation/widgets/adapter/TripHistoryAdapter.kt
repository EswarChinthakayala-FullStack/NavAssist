package com.navassist.android.presentation.widgets.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.navassist.android.R
import com.navassist.android.core.utils.CurrencyUtils
import com.navassist.android.core.utils.LocationUtils
import com.navassist.android.databinding.ItemBookingBinding
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus
import com.navassist.android.presentation.common.base.BaseListAdapter

class TripHistoryAdapter(
    private val onItemClick: (Booking) -> Unit
) : BaseListAdapter<Booking, ItemBookingBinding>(TripDiffCallback()) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemBookingBinding {
        return ItemBookingBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemBookingBinding, item: Booking, position: Int) {
        val displayId = if (item.id.length > 8) item.id.take(8) else item.id
        binding.tvBookingId.text = "Booking #$displayId"

        // Bind Status Chip
        when (item.status) {
            BookingStatus.COMPLETED -> {
                binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_completed)
                binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_check_circle)
                binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#22C55E"))
                binding.tvStatusText.text = "COMPLETED"
                binding.tvStatusText.setTextColor(Color.parseColor("#22C55E"))
            }
            BookingStatus.CANCELLED -> {
                binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_cancelled)
                binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_cancel)
                binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#EF4444"))
                binding.tvStatusText.text = "CANCELLED"
                binding.tvStatusText.setTextColor(Color.parseColor("#EF4444"))
            }
            else -> {
                binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_pending)
                binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_schedule)
                binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#F59E0B"))
                binding.tvStatusText.text = item.status.name
                binding.tvStatusText.setTextColor(Color.parseColor("#F59E0B"))
            }
        }

        binding.tvPickup.text = LocationUtils.formatShortAddress(item.pickupLocation.addressName)
        binding.tvDestination.text = LocationUtils.formatShortAddress(item.destinationLocation.addressName)
        binding.tvFare.text = CurrencyUtils.formatInr(item.fare)
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private class TripDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
    }
}
