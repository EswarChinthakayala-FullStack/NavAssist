package com.navassist.android.presentation.booking.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R
import com.navassist.android.domain.model.PaymentMethod

class PaymentMethodAdapter(
    private val onMethodSelect: (PaymentMethod) -> Unit
) : ListAdapter<PaymentMethod, PaymentMethodAdapter.PaymentMethodViewHolder>(PaymentMethodDiffCallback()) {

    private var selectedId: String = "upi"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_method, parent, false)
        return PaymentMethodViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val item = getItem(position)
        val isSelected = item.id == selectedId
        holder.bind(item, isSelected) {
            selectedId = item.id
            notifyDataSetChanged()
            onMethodSelect(item)
        }
    }

    class PaymentMethodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.cardPaymentOption)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvSubtitle)
        private val vIndicator: ImageView = itemView.findViewById(R.id.vSelectionIndicator)

        fun bind(method: PaymentMethod, isSelected: Boolean, onSelect: () -> Unit) {
            tvTitle.text = method.title
            tvSubtitle.text = method.subtitle
            ivIcon.setImageResource(method.iconResId)

            if (isSelected) {
                card.setCardBackgroundColor(Color.parseColor("#242428"))
                card.strokeColor = Color.parseColor("#FFFFFF")
                card.strokeWidth = (2 * itemView.context.resources.displayMetrics.density).toInt()
                vIndicator.visibility = View.VISIBLE
            } else {
                card.setCardBackgroundColor(Color.parseColor("#18181B"))
                card.strokeColor = Color.parseColor("#303038")
                card.strokeWidth = (1 * itemView.context.resources.displayMetrics.density).toInt()
                vIndicator.visibility = View.GONE
            }

            itemView.setOnClickListener { onSelect() }
        }
    }

    private class PaymentMethodDiffCallback : DiffUtil.ItemCallback<PaymentMethod>() {
        override fun areItemsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PaymentMethod, newItem: PaymentMethod): Boolean {
            return oldItem == newItem
        }
    }
}
