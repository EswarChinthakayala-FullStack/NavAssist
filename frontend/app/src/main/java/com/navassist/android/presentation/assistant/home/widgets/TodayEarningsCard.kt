package com.navassist.android.presentation.assistant.home.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.domain.model.TodayEarnings

class TodayEarningsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvTotalEarnings: TextView
    private val tvTripsCount: TextView
    private val tvAvgFare: TextView
    private val pbWeeklyProgress: ProgressBar
    private val tvProgressLabel: TextView

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

        // Header Row
        val tvHeader = TextView(context).apply {
            text = "TODAY'S EARNINGS SUMMARY"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        // Primary Amount
        tvTotalEarnings = TextView(context).apply {
            text = "₹0.00"
            textSize = 32f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        // Sub Stats Row
        val subStatsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val col1 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl1 = TextView(context).apply { text = "Completed Trips"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvTripsCount = TextView(context).apply { text = "0"; textSize = 16f; setTextColor(Color.parseColor("#FAFAFA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col1.addView(tvLbl1); col1.addView(tvTripsCount)

        val col2 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl2 = TextView(context).apply { text = "Average Fare"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvAvgFare = TextView(context).apply { text = "₹0"; textSize = 16f; setTextColor(Color.parseColor("#FAFAFA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col2.addView(tvLbl2); col2.addView(tvAvgFare)

        subStatsRow.addView(col1)
        subStatsRow.addView(col2)

        // Progress Section
        val progressHeader = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (16 * density).toInt(), 0, (6 * density).toInt())
        }
        val tvProgTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Weekly Goal Progress"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
        }
        tvProgressLabel = TextView(context).apply {
            text = "0%"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        progressHeader.addView(tvProgTitle)
        progressHeader.addView(tvProgressLabel)

        pbWeeklyProgress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (8 * density).toInt())
            max = 100
            progress = 0
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
        }

        rootLayout.addView(tvHeader)
        rootLayout.addView(tvTotalEarnings)
        rootLayout.addView(subStatsRow)
        rootLayout.addView(progressHeader)
        rootLayout.addView(pbWeeklyProgress)

        addView(rootLayout)
    }

    fun bindEarnings(earnings: TodayEarnings) {
        animateValue(tvTotalEarnings, 0.0, earnings.todayEarningsInr, "₹%.2f")
        tvTripsCount.text = "${earnings.completedTripsToday}"
        tvAvgFare.text = "₹${earnings.averageFareInr.toInt()}"

        val pct = earnings.weeklyProgressPct.toInt().coerceIn(0, 100)
        pbWeeklyProgress.progress = pct
        tvProgressLabel.text = "$pct%"
    }

    private fun animateValue(tv: TextView, start: Double, end: Double, formatPattern: String) {
        val anim = ValueAnimator.ofFloat(start.toFloat(), end.toFloat()).apply {
            duration = 900
            addUpdateListener { animation ->
                val v = (animation.animatedValue as Float).toDouble()
                tv.text = String.format(formatPattern, v)
            }
        }
        anim.start()
    }
}
