package com.navassist.android.presentation.wallet.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class WalletBalanceCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvAvailableBalance: TextView
    private val tvPendingBalance: TextView
    private val tvCashbackBalance: TextView
    private val tvLastUpdated: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvHeader = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "AVAILABLE WALLET BALANCE"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvLastUpdated = TextView(context).apply {
            text = "Updated: Just now"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
        }

        topRow.addView(tvHeader)
        topRow.addView(tvLastUpdated)

        tvAvailableBalance = TextView(context).apply {
            text = "₹0.00"
            textSize = 38f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val divider = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                bottomMargin = (14 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        val statsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val col1 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl1 = TextView(context).apply { text = "Pending Funds"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvPendingBalance = TextView(context).apply { text = "₹0.00"; textSize = 16f; setTextColor(Color.parseColor("#FAFAFA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col1.addView(tvLbl1); col1.addView(tvPendingBalance)

        val col2 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl2 = TextView(context).apply { text = "Cashback Rewards"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvCashbackBalance = TextView(context).apply { text = "₹0.00"; textSize = 16f; setTextColor(Color.parseColor("#22C55E")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col2.addView(tvLbl2); col2.addView(tvCashbackBalance)

        statsRow.addView(col1)
        statsRow.addView(col2)

        rootLayout.addView(topRow)
        rootLayout.addView(tvAvailableBalance)
        rootLayout.addView(divider)
        rootLayout.addView(statsRow)

        addView(rootLayout)
    }

    fun bindBalance(available: Double, pending: Double, cashback: Double, lastUpdatedStr: String? = null) {
        animateValue(tvAvailableBalance, 0.0, available, "₹%.2f")
        tvPendingBalance.text = "₹%.2f".format(pending)
        tvCashbackBalance.text = "₹%.2f".format(cashback)
        if (!lastUpdatedStr.isNull_or_empty()) {
            tvLastUpdated.text = "Updated: $lastUpdatedStr"
        }
    }

    private fun animateValue(tv: TextView, start: Double, end: Double, formatPattern: String) {
        val anim = ValueAnimator.ofFloat(start.toFloat(), end.toFloat()).apply {
            duration = 800
            addUpdateListener { animation ->
                val v = (animation.animatedValue as Float).toDouble()
                tv.text = String.format(formatPattern, v)
            }
        }
        anim.start()
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
