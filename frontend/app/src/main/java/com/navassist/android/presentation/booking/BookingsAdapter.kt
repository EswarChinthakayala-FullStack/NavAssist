package com.navassist.android.presentation.booking

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R
import com.navassist.android.core.utils.CurrencyUtils
import com.navassist.android.core.utils.LocationUtils
import com.navassist.android.databinding.ItemBookingBinding
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus

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
            val context = binding.root.context

            // 1. Booking ID Badge
            val displayId = if (booking.id.length > 8) booking.id.take(8) else booking.id
            binding.tvBookingId.text = "Booking #$displayId"

            // 2. Material 3 Status Chip
            bindStatusChip(booking.status)

            // 3. Shortened Locality Addresses
            binding.tvPickup.text = LocationUtils.formatShortAddress(booking.pickupLocation.addressName)
            binding.tvDestination.text = LocationUtils.formatShortAddress(booking.destinationLocation.addressName)

            // 4. Meta Row (Date, Time, Assistant)
            binding.tvDate.text = if (booking.createdAt.isNotBlank()) booking.createdAt.take(10) else "Today"
            binding.tvTime.text = "${booking.estimatedMinutes} min trip"
            binding.tvAssistant.text = booking.assistantName ?: "Verified Assistant"

            // 5. Total Fare in Indian Rupee (₹)
            binding.tvFare.text = CurrencyUtils.formatInr(booking.fare)

            // 6. Click action with smooth press scale animation
            val clickAction = {
                binding.cardBooking.animate()
                    .scaleX(0.98f)
                    .scaleY(0.98f)
                    .setDuration(80)
                    .withEndAction {
                        binding.cardBooking.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .withEndAction {
                                onItemClick(booking)
                            }
                            .start()
                    }
                    .start()
            }

            binding.cardBooking.setOnClickListener { clickAction() }
            binding.btnViewDetails.setOnClickListener { clickAction() }
        }

        private fun bindStatusChip(status: BookingStatus) {
            val context = binding.root.context
            when (status) {
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
                else -> { // PENDING, ACCEPTED, ONGOING
                    binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_pending)
                    binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_schedule)
                    binding.ivStatusIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#F59E0B"))
                    binding.tvStatusText.text = status.name
                    binding.tvStatusText.setTextColor(Color.parseColor("#F59E0B"))
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
        }
    }
}
