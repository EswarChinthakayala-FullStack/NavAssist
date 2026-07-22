package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class ConnectionStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val statusTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (12 * density).toInt()
        val paddingVerticalPx = (4 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)

        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#16341E"))
            cornerRadius = 12f * density
        }
        background = backgroundDrawable

        statusTextView = TextView(context).apply {
            text = "● Live Tracking Connected"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(statusTextView)
    }

    fun setConnected(isConnected: Boolean) {
        if (isConnected) {
            statusTextView.text = "● Live Tracking Connected"
            statusTextView.setTextColor(Color.parseColor("#22C55E"))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#16341E"))
                cornerRadius = 12f * context.resources.displayMetrics.density
            }
        } else {
            statusTextView.text = "● Reconnecting WebSocket..."
            statusTextView.setTextColor(Color.parseColor("#F59E0B"))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#2A2114"))
                cornerRadius = 12f * context.resources.displayMetrics.density
            }
        }
    }
}
