package com.navassist.android.presentation.assistant.earnings.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class WeeklyProgressCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
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
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, (8 * density).toInt())
        }

        val tvTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "WEEKLY EARNINGS TARGET"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvProgressLabel = TextView(context).apply {
            text = "₹3,250 / ₹5,000"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        topRow.addView(tvTitle)
        topRow.addView(tvProgressLabel)

        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (8 * density).toInt())
            max = 100
            progress = 65
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
        }

        rootLayout.addView(topRow)
        rootLayout.addView(progressBar)

        addView(rootLayout)
    }

    fun setProgress(weeklyEarned: Double, target: Double = 5000.0) {
        val pct = ((weeklyEarned / target) * 100).toInt().coerceIn(0, 100)
        progressBar.progress = pct
        tvProgressLabel.text = "₹%.0f / ₹%.0f".format(weeklyEarned, target)
    }
}
