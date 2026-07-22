package com.navassist.android.presentation.assistant.earnings.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class PayoutCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvPayoutAmount: TextView
    private val tvPayoutDate: TextView
    private val tvPayoutStatus: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvIcon = TextView(context).apply {
            text = "🏦 "
            textSize = 24f
            setPadding(0, 0, (12 * density).toInt(), 0)
        }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvHeader = TextView(context).apply {
            text = "NEXT SCHEDULED PAYOUT"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvPayoutAmount = TextView(context).apply {
            text = "₹0.00"
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        tvPayoutDate = TextView(context).apply {
            text = "Expected: Sunday, 26 Jul 2026"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        colText.addView(tvHeader)
        colText.addView(tvPayoutAmount)
        colText.addView(tvPayoutDate)

        tvPayoutStatus = TextView(context).apply {
            text = "Scheduled"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (4 * density).toInt()
            val pH = (10 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        rootLayout.addView(tvIcon)
        rootLayout.addView(colText)
        rootLayout.addView(tvPayoutStatus)

        addView(rootLayout)
    }

    fun bindPayout(amount: Double, dateStr: String, statusStr: String) {
        tvPayoutAmount.text = "₹%.2f".format(amount)
        tvPayoutDate.text = "Expected: $dateStr"
        tvPayoutStatus.text = statusStr

        when (statusStr.uppercase()) {
            "COMPLETED" -> {
                tvPayoutStatus.setTextColor(Color.parseColor("#22C55E"))
                tvPayoutStatus.setBackgroundColor(Color.parseColor("#2622C55E"))
            }
            "PROCESSING" -> {
                tvPayoutStatus.setTextColor(Color.parseColor("#F59E0B"))
                tvPayoutStatus.setBackgroundColor(Color.parseColor("#26F59E0B"))
            }
            "FAILED" -> {
                tvPayoutStatus.setTextColor(Color.parseColor("#EF4444"))
                tvPayoutStatus.setBackgroundColor(Color.parseColor("#26EF4444"))
            }
            else -> {
                tvPayoutStatus.setTextColor(Color.parseColor("#22C55E"))
                tvPayoutStatus.setBackgroundColor(Color.parseColor("#2622C55E"))
            }
        }
    }
}
