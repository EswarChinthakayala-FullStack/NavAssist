package com.navassist.android.presentation.assistant.earnings.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class EarningsSummaryCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvLifetimeEarnings: TextView
    private val tvTodayEarnings: TextView
    private val tvWeeklyEarnings: TextView

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

        val tvHeader = TextView(context).apply {
            text = "LIFETIME TOTAL EARNINGS"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvLifetimeEarnings = TextView(context).apply {
            text = "₹0.00"
            textSize = 36f
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
            gravity = Gravity.CENTER_VERTICAL
        }

        val col1 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl1 = TextView(context).apply { text = "Today's Income"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvTodayEarnings = TextView(context).apply { text = "₹0"; textSize = 18f; setTextColor(Color.parseColor("#FAFAFA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col1.addView(tvLbl1); col1.addView(tvTodayEarnings)

        val col2 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvLbl2 = TextView(context).apply { text = "This Week"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvWeeklyEarnings = TextView(context).apply { text = "₹0"; textSize = 18f; setTextColor(Color.parseColor("#FAFAFA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col2.addView(tvLbl2); col2.addView(tvWeeklyEarnings)

        statsRow.addView(col1)
        statsRow.addView(col2)

        rootLayout.addView(tvHeader)
        rootLayout.addView(tvLifetimeEarnings)
        rootLayout.addView(divider)
        rootLayout.addView(statsRow)

        addView(rootLayout)
    }

    fun bindSummary(lifetime: Double, today: Double, weekly: Double) {
        animateValue(tvLifetimeEarnings, 0.0, lifetime, "₹%.2f")
        animateValue(tvTodayEarnings, 0.0, today, "₹%.0f")
        animateValue(tvWeeklyEarnings, 0.0, weekly, "₹%.0f")
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
}
