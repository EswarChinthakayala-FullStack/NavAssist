package com.navassist.android.presentation.wallet.adapter

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R
import com.navassist.android.domain.model.TransactionType
import com.navassist.android.domain.model.WalletTransaction
import com.navassist.android.presentation.wallet.widgets.TransactionStatusChip

class WalletTransactionAdapter(
    private val onItemClick: (WalletTransaction) -> Unit
) : ListAdapter<WalletTransaction, WalletTransactionAdapter.TransactionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val density = parent.context.resources.displayMetrics.density
        val card = MaterialCardView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = (10 * density).toInt()
            }
            radius = (16 * density)
            setCardBackgroundColor(Color.parseColor("#18181B"))
            strokeColor = Color.parseColor("#27272A")
            strokeWidth = (1.5f * density).toInt()
            cardElevation = 0f

            val layout = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                val pad = (14 * density).toInt()
                setPadding(pad, pad, pad, pad)
            }

            val ivIcon = ImageView(parent.context).apply {
                id = 801
                val size = (36 * density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    marginEnd = (12 * density).toInt()
                }
                setImageResource(R.drawable.ic_feature_payments)
            }

            val colText = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tvDesc = TextView(parent.context).apply {
                id = 802
                textSize = 14f
                setTextColor(Color.parseColor("#FAFAFA"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val tvTime = TextView(parent.context).apply {
                id = 803
                textSize = 12f
                setTextColor(Color.parseColor("#71717A"))
                setPadding(0, (2 * density).toInt(), 0, 0)
            }

            colText.addView(tvDesc)
            colText.addView(tvTime)

            val colAmount = LinearLayout(parent.context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END
            }

            val tvAmount = TextView(parent.context).apply {
                id = 804
                textSize = 15f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }

            val chipStatus = TransactionStatusChip(parent.context).apply {
                id = 805
            }

            colAmount.addView(tvAmount)
            colAmount.addView(chipStatus)

            layout.addView(ivIcon)
            layout.addView(colText)
            layout.addView(colAmount)

            addView(layout)
        }
        return TransactionViewHolder(card)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(val card: MaterialCardView) : RecyclerView.ViewHolder(card) {
        fun bind(item: WalletTransaction) {
            val isCredit = item.type == TransactionType.CREDIT
            card.findViewById<TextView>(802).text = item.description
            card.findViewById<TextView>(803).text = item.timestamp

            val tvAmount = card.findViewById<TextView>(804)
            if (isCredit) {
                tvAmount.text = "+₹%.2f".format(item.amount)
                tvAmount.setTextColor(Color.parseColor("#22C55E"))
            } else {
                tvAmount.text = "-₹%.2f".format(item.amount)
                tvAmount.setTextColor(Color.parseColor("#FAFAFA"))
            }

            val chip = card.findViewById<TransactionStatusChip>(805)
            chip.setStatus(item.status)

            card.setOnClickListener { onItemClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WalletTransaction>() {
        override fun areItemsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction): Boolean = oldItem == newItem
    }
}
