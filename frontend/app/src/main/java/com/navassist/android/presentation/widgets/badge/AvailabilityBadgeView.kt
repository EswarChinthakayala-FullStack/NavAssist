package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class AvailabilityBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val statusTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (10 * density).toInt()
        val paddingVerticalPx = (4 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)

        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#242428"))
            setStroke((1 * density).toInt(), Color.parseColor("#303038"))
            cornerRadius = 12f * density
        }
        background = backgroundDrawable

        statusTextView = TextView(context).apply {
            text = "AVAILABLE"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(statusTextView)
    }

    fun setAvailability(isAvailable: Boolean) {
        if (isAvailable) {
            statusTextView.text = "AVAILABLE"
            statusTextView.setTextColor(Color.parseColor("#22C55E"))
        } else {
            statusTextView.text = "BUSY"
            statusTextView.setTextColor(Color.parseColor("#EF4444"))
        }
    }
}
