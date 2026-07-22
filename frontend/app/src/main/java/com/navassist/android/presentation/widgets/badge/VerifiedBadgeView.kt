package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class VerifiedBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (10 * density).toInt()
        val paddingVerticalPx = (4 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)

        val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
            setColor(Color.parseColor("#FFFFFF"))
            cornerRadius = 12f * density
        }
        background = backgroundDrawable

        val badgeText = TextView(context).apply {
            text = "✔ Verified"
            setTextColor(Color.parseColor("#09090B"))
            textSize = 11f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(badgeText)
    }
}
