package com.navassist.android.presentation.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.databinding.ItemBookingBinding
import com.navassist.android.domain.model.Booking

class BookingsAdapter(
    private val onItemClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingsAdapter.BookingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(
        private val binding: ItemBookingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvBookingId.text = "Booking #${booking.id.take(8)}"
            binding.statusBadge.setStatus(booking.status.name)
            binding.tvPickup.text = "From: ${booking.pickupLocation.addressName ?: "Pickup Point"}"
            binding.tvDestination.text = "To: ${booking.destinationLocation.addressName ?: "Destination Point"}"
            binding.tvFare.text = "$${String.format("%.2f", booking.fare)}"

            binding.root.setOnClickListener { onItemClick(booking) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
        }
    }
}
