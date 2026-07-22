package com.navassist.android.presentation.assistant.home.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.domain.model.Booking
import com.navassist.android.presentation.assistant.home.widgets.BookingRequestCard

class IncomingBookingAdapter(
    private val onAccept: (Booking) -> Unit,
    private val onDecline: (Booking) -> Unit
) : ListAdapter<Booking, IncomingBookingAdapter.BookingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val card = BookingRequestCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * parent.context.resources.displayMetrics.density).toInt()
            }
        }
        return BookingViewHolder(card)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = getItem(position)
        holder.bind(booking)
    }

    inner class BookingViewHolder(val card: BookingRequestCard) : RecyclerView.ViewHolder(card) {
        fun bind(booking: Booking) {
            card.bindBooking(booking)
            card.onAcceptClickListener = { onAccept(booking) }
            card.onDeclineClickListener = { onDecline(booking) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
    }
}
