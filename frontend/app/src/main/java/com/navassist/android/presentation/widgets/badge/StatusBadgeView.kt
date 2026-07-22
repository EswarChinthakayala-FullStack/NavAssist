package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class StatusBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val statusTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (14 * density).toInt()
        val paddingVerticalPx = (6 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)

        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#18181B"))
            setStroke((1 * density).toInt(), Color.parseColor("#FFFFFF"))
            cornerRadius = 14f * density
        }
        background = backgroundDrawable

        statusTextView = TextView(context).apply {
            text = "● ASSIGNED"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(statusTextView)
    }

    fun setStatus(status: String) {
        statusTextView.text = "● ${status.uppercase()}"
    }
}
