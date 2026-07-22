package com.navassist.android.presentation.profile.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class VerificationStatusCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val tvStatusBadge: TextView
    val tvTrustScore: TextView
    val tvRating: TextView

    var onVerificationClickListener: (() -> Unit)? = null

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "GOVERNMENT IDENTITY & KYC"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvStatusBadge = TextView(context).apply {
            text = "VERIFIED ✓"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        topRow.addView(tvTitle)
        topRow.addView(tvStatusBadge)

        val statsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (12 * density).toInt(), 0, 0)
        }

        tvTrustScore = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "🛡️ Trust Score: 98%"
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvRating = TextView(context).apply {
            text = "⭐ 5.0 Rating"
            textSize = 13f
            setTextColor(Color.parseColor("#F59E0B"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        statsRow.addView(tvTrustScore)
        statsRow.addView(tvRating)

        rootLayout.addView(topRow)
        rootLayout.addView(statsRow)

        setOnClickListener { onVerificationClickListener?.invoke() }

        addView(rootLayout)
    }

    fun bindVerification(statusStr: String, trustScore: Float, rating: Float) {
        tvTrustScore.text = "🛡️ Trust Score: %.0f%%".format(trustScore)
        tvRating.text = "⭐ %.1f Rating".format(rating)

        when (statusStr.uppercase()) {
            "VERIFIED", "APPROVED" -> {
                tvStatusBadge.text = "VERIFIED ✓"
                tvStatusBadge.setTextColor(Color.parseColor("#22C55E"))
                tvStatusBadge.setBackgroundColor(Color.parseColor("#2622C55E"))
            }
            "PENDING" -> {
                tvStatusBadge.text = "UNDER REVIEW ⏱️"
                tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"))
                tvStatusBadge.setBackgroundColor(Color.parseColor("#26F59E0B"))
            }
            "REJECTED" -> {
                tvStatusBadge.text = "ACTION REQUIRED ⚠️"
                tvStatusBadge.setTextColor(Color.parseColor("#EF4444"))
                tvStatusBadge.setBackgroundColor(Color.parseColor("#26EF4444"))
            }
            else -> {
                tvStatusBadge.text = "NOT SUBMITTED"
                tvStatusBadge.setTextColor(Color.parseColor("#71717A"))
                tvStatusBadge.setBackgroundColor(Color.parseColor("#27272A"))
            }
        }
    }
}
