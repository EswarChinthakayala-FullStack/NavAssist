package com.navassist.android.presentation.assistant.earnings.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class EarningsAnalyticsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val chartView: IncomeTrendChart
    private val tvPeakDay: TextView
    private val tvAvgDaily: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
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
            text = "REVENUE TREND & ANALYTICS"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (12 * density).toInt())
        }

        chartView = IncomeTrendChart(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (100 * density).toInt()
            )
        }

        val footerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (12 * density).toInt(), 0, 0)
        }

        tvPeakDay = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "🔥 Peak Day: Sunday (₹3,100)"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        tvAvgDaily = TextView(context).apply {
            text = "📊 Avg: ₹2,150/day"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        footerRow.addView(tvPeakDay)
        footerRow.addView(tvAvgDaily)

        rootLayout.addView(tvTitle)
        rootLayout.addView(chartView)
        rootLayout.addView(footerRow)

        addView(rootLayout)
    }
}
