package com.navassist.android.presentation.admin.sos.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.data.remote.dto.sos.SosResponseDto
import com.navassist.android.presentation.admin.sos.widgets.EmergencyCard

class SosAlertsAdapter(
    private val onResolveClick: (SosResponseDto) -> Unit,
    private val onItemClick: (SosResponseDto) -> Unit
) : ListAdapter<SosResponseDto, SosAlertsAdapter.SosViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SosViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = EmergencyCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }
        return SosViewHolder(card)
    }

    override fun onBindViewHolder(holder: SosViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SosViewHolder(val card: EmergencyCard) : RecyclerView.ViewHolder(card) {
        fun bind(item: SosResponseDto) {
            card.bindAlert(item)
            card.onResolveClickListener = { onResolveClick(item) }
            card.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SosResponseDto>() {
        override fun areItemsTheSame(oldItem: SosResponseDto, newItem: SosResponseDto): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SosResponseDto, newItem: SosResponseDto): Boolean = oldItem == newItem
    }
}
