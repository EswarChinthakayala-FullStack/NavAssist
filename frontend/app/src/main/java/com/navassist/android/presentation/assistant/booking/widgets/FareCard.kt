package com.navassist.android.presentation.assistant.booking.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class FareCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvTotalFare: TextView
    private val tvBaseFare: TextView
    private val tvDistanceFare: TextView
    private val tvPlatformFee: TextView
    private val tvNetEarnings: TextView

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

        // Header Row
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val tvTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "TRIP FARE BREAKDOWN"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvTotalFare = TextView(context).apply {
            text = "₹0.00"
            textSize = 24f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        topRow.addView(tvTitle)
        topRow.addView(tvTotalFare)

        // Divider
        val divider1 = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                topMargin = (12 * density).toInt()
                bottomMargin = (12 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        // Breakdown items
        tvBaseFare = createRow(rootLayout, "Base Fare", "₹50.00")
        tvDistanceFare = createRow(rootLayout, "Distance & Time Fare", "₹180.00")
        tvPlatformFee = createRow(rootLayout, "Platform & Safety Fee", "-₹15.00")

        // Divider
        val divider2 = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                topMargin = (8 * density).toInt()
                bottomMargin = (8 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        // Net Earnings
        val netRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (4 * density).toInt(), 0, 0)
        }

        val tvNetTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "YOUR TAKE-HOME EARNINGS"
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvNetEarnings = TextView(context).apply {
            text = "₹215.00"
            textSize = 18f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        netRow.addView(tvNetTitle)
        netRow.addView(tvNetEarnings)

        rootLayout.addView(topRow)
        rootLayout.addView(divider1)
        rootLayout.addView(divider2)
        rootLayout.addView(netRow)

        addView(rootLayout)
    }

    private fun createRow(parent: LinearLayout, label: String, valStr: String): TextView {
        val density = context.resources.displayMetrics.density
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
        }

        val tvLbl = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = label
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        val tvVal = TextView(context).apply {
            text = valStr
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
        }

        row.addView(tvLbl)
        row.addView(tvVal)

        // Insert before second divider
        val insertIndex = parent.childCount - 1
        parent.addView(row, if (insertIndex >= 0) insertIndex else parent.childCount)
        return tvVal
    }

    fun setFare(estimatedFare: Double) {
        val baseFare = 50.0
        val distFare = (estimatedFare - baseFare).coerceAtLeast(0.0)
        val platformFee = 15.0
        val netEarnings = (estimatedFare - platformFee).coerceAtLeast(0.0)

        animateValue(tvTotalFare, 0.0, estimatedFare, "₹%.2f")
        tvBaseFare.text = "₹%.2f".format(baseFare)
        tvDistanceFare.text = "₹%.2f".format(distFare)
        tvPlatformFee.text = "-₹%.2f".format(platformFee)
        animateValue(tvNetEarnings, 0.0, netEarnings, "₹%.2f")
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
