package com.navassist.android.presentation.offers.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.navassist.android.R
import com.navassist.android.domain.model.Coupon
import com.navassist.android.presentation.widgets.badge.EligibilityStatusView
import com.navassist.android.presentation.widgets.badge.OfferBadgeView

class CouponAdapter(
    private val onCouponSelect: (Coupon) -> Unit
) : ListAdapter<Coupon, CouponAdapter.CouponViewHolder>(CouponDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(getItem(position), onCouponSelect)
    }

    class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val badgeOffer: OfferBadgeView = itemView.findViewById(R.id.badgeOffer)
        private val tvCode: TextView = itemView.findViewById(R.id.tvCouponCode)
        private val tvDiscount: TextView = itemView.findViewById(R.id.tvDiscountValue)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvCouponTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvCouponDesc)
        private val eligibilityStatus: EligibilityStatusView = itemView.findViewById(R.id.eligibilityStatus)
        private val btnApply: MaterialButton = itemView.findViewById(R.id.btnApplyCoupon)

        fun bind(coupon: Coupon, onSelect: (Coupon) -> Unit) {
            badgeOffer.setBadgeText(coupon.badgeText)
            tvCode.text = coupon.code
            tvDiscount.text = coupon.discountText
            tvTitle.text = coupon.title
            tvDesc.text = coupon.description
            eligibilityStatus.setEligible(coupon.isEligible, coupon.minBookingAmount)

            btnApply.isEnabled = coupon.isEligible
            btnApply.setOnClickListener {
                onSelect(coupon)
            }
        }
    }

    private class CouponDiffCallback : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem == newItem
        }
    }
}
