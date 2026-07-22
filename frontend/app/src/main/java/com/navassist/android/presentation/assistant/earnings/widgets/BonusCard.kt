package com.navassist.android.presentation.assistant.earnings.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class BonusCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvIncentives: TextView
    private val tvBonuses: TextView

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

        val tvTitle = TextView(context).apply {
            text = "INCENTIVES & BONUSES"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (10 * density).toInt())
        }

        val row1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
        }
        val tvLbl1 = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "🎁 Peak Hour Incentives"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
        }
        tvIncentives = TextView(context).apply {
            text = "+₹250.00"
            textSize = 14f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        row1.addView(tvLbl1); row1.addView(tvIncentives)

        val row2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
        }
        val tvLbl2 = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "⭐ Weekly Target Bonus"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
        }
        tvBonuses = TextView(context).apply {
            text = "+₹100.00"
            textSize = 14f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        row2.addView(tvLbl2); row2.addView(tvBonuses)

        rootLayout.addView(tvTitle)
        rootLayout.addView(row1)
        rootLayout.addView(row2)

        addView(rootLayout)
    }

    fun bindRewards(incentives: Double, bonuses: Double) {
        tvIncentives.text = "+₹%.2f".format(incentives)
        tvBonuses.text = "+₹%.2f".format(bonuses)
    }
}
