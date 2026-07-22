package com.navassist.android.presentation.widgets.chip

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class TrackingStatusChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val chipTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (14 * density).toInt()
        val paddingVerticalPx = (6 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)

        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#FFFFFF"))
            cornerRadius = 16f * density
        }
        background = backgroundDrawable

        chipTextView = TextView(context).apply {
            text = "● Live Tracking Connected"
            setTextColor(Color.parseColor("#09090B"))
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(chipTextView)
    }

    fun setStatus(isConnected: Boolean) {
        if (isConnected) {
            chipTextView.text = "● Live Tracking Connected"
            chipTextView.setTextColor(Color.parseColor("#09090B"))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#FFFFFF"))
                cornerRadius = 16f * context.resources.displayMetrics.density
            }
        } else {
            chipTextView.text = "● Reconnecting..."
            chipTextView.setTextColor(Color.parseColor("#FAFAFA"))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#18181B"))
                setStroke((1 * context.resources.displayMetrics.density).toInt(), Color.parseColor("#303038"))
                cornerRadius = 16f * context.resources.displayMetrics.density
            }
        }
    }
}
