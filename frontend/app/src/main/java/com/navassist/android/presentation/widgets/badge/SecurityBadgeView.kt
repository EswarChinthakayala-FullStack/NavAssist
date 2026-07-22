package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class SecurityBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#27272A"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#3F3F46")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val titleView = TextView(context).apply {
            text = "Your Safety Matters"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        val descView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (6 * density).toInt()
            }
            text = "All NavAssist travel assistants undergo comprehensive government ID verification, criminal background checks, and active trip monitoring to ensure total peace of mind."
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
            setLineSpacing(4f, 1f)
        }

        container.addView(titleView)
        container.addView(descView)
        addView(container)
    }
}
