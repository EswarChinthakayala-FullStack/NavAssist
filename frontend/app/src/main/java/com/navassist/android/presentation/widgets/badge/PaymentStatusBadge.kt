package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class PaymentStatusBadge @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (12 * density).toInt()
        val paddingVerticalPx = (6 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)

        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#16341E"))
            setStroke((1 * density).toInt(), Color.parseColor("#22C55E"))
            cornerRadius = 14f * density
        }
        background = backgroundDrawable

        val badgeText = TextView(context).apply {
            text = "✔ VERIFIED & PAID"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(badgeText)
    }
}
