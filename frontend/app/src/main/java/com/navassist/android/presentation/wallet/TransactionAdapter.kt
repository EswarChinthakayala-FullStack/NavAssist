package com.navassist.android.presentation.wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.navassist.android.R
import com.navassist.android.databinding.ItemTransactionBinding
import com.navassist.android.domain.model.TransactionType
import com.navassist.android.domain.model.WalletTransaction

class TransactionAdapter : ListAdapter<WalletTransaction, TransactionAdapter.TransactionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WalletTransaction) {
            binding.tvDescription.text = item.description
            binding.tvTimestamp.text = item.timestamp
            val prefix = if (item.type == TransactionType.CREDIT) "+" else "-"
            binding.tvAmount.text = "$prefix$${String.format("%.2f", item.amount)}"
            val color = if (item.type == TransactionType.CREDIT) R.color.accent_emerald_dark else R.color.sos_red
            binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, color))
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WalletTransaction>() {
            override fun areItemsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction): Boolean = oldItem == newItem
        }
    }
}
