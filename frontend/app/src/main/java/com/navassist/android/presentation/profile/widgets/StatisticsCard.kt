package com.navassist.android.presentation.profile.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class StatisticsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val tvRating: TextView
    val tvTotalTrips: TextView
    val tvTrustScore: TextView
    val tvAcceptanceRate: TextView

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
            text = "ASSISTANT PERFORMANCE METRICS"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (12 * density).toInt())
        }

        val row1 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val col1 = createMetricCol("Rating", "5.0 ★").also { tvRating = it.second }
        val col2 = createMetricCol("Total Trips", "0").also { tvTotalTrips = it.second }

        row1.addView(col1.first)
        row1.addView(col2.first)

        val row2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (12 * density).toInt(), 0, 0)
        }

        val col3 = createMetricCol("Trust Score", "98%").also { tvTrustScore = it.second }
        val col4 = createMetricCol("Acceptance Rate", "96%").also { tvAcceptanceRate = it.second }

        row2.addView(col3.first)
        row2.addView(col4.first)

        rootLayout.addView(tvTitle)
        rootLayout.addView(row1)
        rootLayout.addView(row2)

        addView(rootLayout)
    }

    private fun createMetricCol(label: String, initialVal: String): Pair<LinearLayout, TextView> {
        val density = context.resources.displayMetrics.density
        val col = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl = TextView(context).apply {
            text = label
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
        }
        val tvVal = TextView(context).apply {
            text = initialVal
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (2 * density).toInt(), 0, 0)
        }
        col.addView(tvLbl)
        col.addView(tvVal)
        return Pair(col, tvVal)
    }

    fun bindMetrics(rating: Float, trips: Int, trustScore: Float) {
        tvRating.text = "%.1f ★".format(rating)
        tvTotalTrips.text = trips.toString()
        tvTrustScore.text = "%.0f%%".format(trustScore)
    }
}
