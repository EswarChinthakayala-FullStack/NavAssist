package com.navassist.android.presentation.assistant.earnings.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.domain.model.TripEarningsItem
import com.navassist.android.presentation.assistant.earnings.widgets.TripEarningCard

class EarningsHistoryAdapter(
    private val onItemClick: (TripEarningsItem) -> Unit
) : ListAdapter<TripEarningsItem, EarningsHistoryAdapter.EarningsViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EarningsViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = TripEarningCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }
        return EarningsViewHolder(card)
    }

    override fun onBindViewHolder(holder: EarningsViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class EarningsViewHolder(val card: TripEarningCard) : RecyclerView.ViewHolder(card) {
        fun bind(item: TripEarningsItem) {
            card.bindTrip(item)
            card.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TripEarningsItem>() {
        override fun areItemsTheSame(oldItem: TripEarningsItem, newItem: TripEarningsItem): Boolean = oldItem.bookingId == newItem.bookingId
        override fun areContentsTheSame(oldItem: TripEarningsItem, newItem: TripEarningsItem): Boolean = oldItem == newItem
    }
}
