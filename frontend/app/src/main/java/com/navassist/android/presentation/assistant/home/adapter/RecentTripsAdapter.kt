package com.navassist.android.presentation.assistant.home.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.domain.model.Booking

class RecentTripsAdapter(
    private val onTripClick: (Booking) -> Unit
) : ListAdapter<Booking, RecentTripsAdapter.TripViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = MaterialCardView(parent.context).apply {
            val w = (200 * density).toInt()
            layoutParams = RecyclerView.LayoutParams(w, RecyclerView.LayoutParams.WRAP_CONTENT).apply {
                marginEnd = (12 * density).toInt()
            }
            radius = (16 * density)
            setCardBackgroundColor(Color.parseColor("#18181B"))
            strokeColor = Color.parseColor("#27272A")
            strokeWidth = (1f * density).toInt()
            cardElevation = 0f

            val layout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                val pad = (14 * density).toInt()
                setPadding(pad, pad, pad, pad)
            }

            val tvPassenger = TextView(parent.context).apply {
                id = 301
                textSize = 14f
                setTextColor(Color.parseColor("#FAFAFA"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val tvFare = TextView(parent.context).apply {
                id = 302
                textSize = 16f
                setTextColor(Color.parseColor("#22C55E"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, (4 * density).toInt(), 0, 0)
            }

            val tvDest = TextView(parent.context).apply {
                id = 303
                textSize = 12f
                setTextColor(Color.parseColor("#A1A1AA"))
                maxLines = 1
                setPadding(0, (4 * density).toInt(), 0, 0)
            }

            layout.addView(tvPassenger)
            layout.addView(tvFare)
            layout.addView(tvDest)
            addView(layout)
        }
        return TripViewHolder(card)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TripViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        fun bind(booking: Booking) {
            val tvPassenger = card.findViewById<TextView>(301)
            val tvFare = card.findViewById<TextView>(302)
            val tvDest = card.findViewById<TextView>(303)

            tvPassenger.text = booking.guestName
            tvFare.text = "₹${booking.fare.toInt()}"
            tvDest.text = booking.destinationLocation.address

            card.setOnClickListener { onTripClick(booking) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean = oldItem == newItem
    }
}
