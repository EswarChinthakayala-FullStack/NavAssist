package com.navassist.android.presentation.admin.bookings.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.data.remote.api.AdminBookingDto
import com.navassist.android.presentation.admin.bookings.widgets.AdminBookingCard

class AdminBookingsAdapter(
    private val onItemClick: (AdminBookingDto) -> Unit
) : ListAdapter<AdminBookingDto, AdminBookingsAdapter.BookingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = AdminBookingCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }
        return BookingViewHolder(card)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(val card: AdminBookingCard) : RecyclerView.ViewHolder(card) {
        fun bind(item: AdminBookingDto) {
            card.bindBooking(item)
            card.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AdminBookingDto>() {
        override fun areItemsTheSame(oldItem: AdminBookingDto, newItem: AdminBookingDto): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AdminBookingDto, newItem: AdminBookingDto): Boolean = oldItem == newItem
    }
}
