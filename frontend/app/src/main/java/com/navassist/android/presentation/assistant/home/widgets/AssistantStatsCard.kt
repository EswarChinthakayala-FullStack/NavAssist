package com.navassist.android.presentation.assistant.home.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.domain.model.AssistantDashboardStats

class AssistantStatsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvTripsVal: TextView
    private val tvEarningsVal: TextView
    private val tvRatingVal: TextView
    private val tvAcceptanceVal: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val grid = GridLayout(context).apply {
            columnCount = 2
            rowCount = 2
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        // Cell 1: Today's Trips
        val cell1 = createStatCell("TODAY'S TRIPS", "0", "#FAFAFA")
        tvTripsVal = cell1.findViewById(101)

        // Cell 2: Today's Earnings
        val cell2 = createStatCell("EARNINGS", "₹0", "#22C55E")
        tvEarningsVal = cell2.findViewById(101)

        // Cell 3: Rating
        val cell3 = createStatCell("RATING", "5.0 ★", "#F59E0B")
        tvRatingVal = cell3.findViewById(101)

        // Cell 4: Acceptance Rate
        val cell4 = createStatCell("ACCEPTANCE", "95%", "#FAFAFA")
        tvAcceptanceVal = cell4.findViewById(101)

        val params1 = GridLayout.LayoutParams().apply {
            width = 0
            columnSpec = GridLayout.spec(0, 1f)
            rowSpec = GridLayout.spec(0)
            bottomMargin = (12 * density).toInt()
            marginEnd = (8 * density).toInt()
        }

        val params2 = GridLayout.LayoutParams().apply {
            width = 0
            columnSpec = GridLayout.spec(1, 1f)
            rowSpec = GridLayout.spec(0)
            bottomMargin = (12 * density).toInt()
            marginStart = (8 * density).toInt()
        }

        val params3 = GridLayout.LayoutParams().apply {
            width = 0
            columnSpec = GridLayout.spec(0, 1f)
            rowSpec = GridLayout.spec(1)
            marginEnd = (8 * density).toInt()
        }

        val params4 = GridLayout.LayoutParams().apply {
            width = 0
            columnSpec = GridLayout.spec(1, 1f)
            rowSpec = GridLayout.spec(1)
            marginStart = (8 * density).toInt()
        }

        grid.addView(cell1, params1)
        grid.addView(cell2, params2)
        grid.addView(cell3, params3)
        grid.addView(cell4, params4)

        addView(grid)
    }

    private fun createStatCell(label: String, initialVal: String, valColor: String): LinearLayout {
        val density = context.resources.displayMetrics.density
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#18181B"))
            val pad = (14 * density).toInt()
            setPadding(pad, pad, pad, pad)

            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 14 * density
                setColor(Color.parseColor("#18181B"))
                setStroke((1 * density).toInt(), Color.parseColor("#27272A"))
            }
            background = drawable

            val tvLbl = TextView(context).apply {
                text = label
                textSize = 11f
                setTextColor(Color.parseColor("#71717A"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                letterSpacing = 0.05f
            }

            val tvVal = TextView(context).apply {
                id = 101
                text = initialVal
                textSize = 20f
                setTextColor(Color.parseColor(valColor))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setPadding(0, (6 * density).toInt(), 0, 0)
            }

            addView(tvLbl)
            addView(tvVal)
        }
    }

    fun bindStats(stats: AssistantDashboardStats, animate: Boolean = true) {
        if (animate) {
            animateCountUpInt(tvTripsVal, 0, stats.todayTrips, "")
            animateCountUpDouble(tvEarningsVal, 0.0, stats.todayEarnings, "₹%.0f")
            tvRatingVal.text = "${String.format("%.1f", stats.rating)} ★"
            animateCountUpDouble(tvAcceptanceVal, 0.0, stats.acceptanceRate, "%.0f%%")
        } else {
            tvTripsVal.text = "${stats.todayTrips}"
            tvEarningsVal.text = "₹${stats.todayEarnings.toInt()}"
            tvRatingVal.text = "${String.format("%.1f", stats.rating)} ★"
            tvAcceptanceVal.text = "${stats.acceptanceRate.toInt()}%"
        }
    }

    private fun animateCountUpInt(tv: TextView, start: Int, end: Int, prefix: String) {
        val anim = ValueAnimator.ofInt(start, end).apply {
            duration = 800
            addUpdateListener { animation ->
                tv.text = "$prefix${animation.animatedValue}"
            }
        }
        anim.start()
    }

    private fun animateCountUpDouble(tv: TextView, start: Double, end: Double, formatPattern: String) {
        val anim = ValueAnimator.ofFloat(start.toFloat(), end.toFloat()).apply {
            duration = 800
            addUpdateListener { animation ->
                val v = (animation.animatedValue as Float).toDouble()
                tv.text = String.format(formatPattern, v)
            }
        }
        anim.start()
    }
}
