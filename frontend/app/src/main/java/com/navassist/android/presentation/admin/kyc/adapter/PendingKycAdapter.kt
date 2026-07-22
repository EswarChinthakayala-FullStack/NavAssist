package com.navassist.android.presentation.admin.kyc.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.data.remote.api.AdminKycItemDto
import com.navassist.android.presentation.admin.kyc.widgets.PendingKycCard

class PendingKycAdapter(
    private val onApproveClick: (AdminKycItemDto) -> Unit,
    private val onRejectClick: (AdminKycItemDto) -> Unit,
    private val onItemClick: (AdminKycItemDto) -> Unit
) : ListAdapter<AdminKycItemDto, PendingKycAdapter.KycViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KycViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = PendingKycCard(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
        }
        return KycViewHolder(card)
    }

    override fun onBindViewHolder(holder: KycViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class KycViewHolder(val card: PendingKycCard) : RecyclerView.ViewHolder(card) {
        fun bind(item: AdminKycItemDto) {
            card.bindItem(item)
            card.onApproveClickListener = { onApproveClick(item) }
            card.onRejectClickListener = { onRejectClick(item) }
            card.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<AdminKycItemDto>() {
        override fun areItemsTheSame(oldItem: AdminKycItemDto, newItem: AdminKycItemDto): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AdminKycItemDto, newItem: AdminKycItemDto): Boolean = oldItem == newItem
    }
}
