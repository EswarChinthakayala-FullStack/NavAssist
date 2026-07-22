package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class EligibilityStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val statusTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        statusTextView = TextView(context).apply {
            text = "✔ Eligible for your booking"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(statusTextView)
    }

    fun setEligible(isEligible: Boolean, minAmount: Double) {
        if (isEligible) {
            statusTextView.text = "✔ Eligible for your booking"
            statusTextView.setTextColor(Color.parseColor("#22C55E"))
        } else {
            statusTextView.text = "ⓘ Requires min. booking ₹${minAmount.toInt()}"
            statusTextView.setTextColor(Color.parseColor("#A1A1AA"))
        }
    }
}
