package com.navassist.android.presentation.widgets.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.navassist.android.databinding.ItemTransactionBinding
import com.navassist.android.domain.model.NotificationItem
import com.navassist.android.presentation.common.base.BaseListAdapter

class NotificationAdapter(
    private val onItemClick: (NotificationItem) -> Unit
) : BaseListAdapter<NotificationItem, ItemTransactionBinding>(NotificationDiffCallback()) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemTransactionBinding {
        return ItemTransactionBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemTransactionBinding, item: NotificationItem, position: Int) {
        binding.tvDescription.text = item.title
        binding.tvTimestamp.text = item.body
        binding.tvAmount.text = if (item.isRead) "READ" else "NEW"
        binding.root.setOnClickListener { onItemClick(item) }
    }

    private class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean = oldItem == newItem
    }
}
