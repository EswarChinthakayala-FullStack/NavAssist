package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class PricingInfoCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val titleView = TextView(context).apply {
            text = "Pricing Transparency & Policies"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        val descView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (6 * density).toInt()
            }
            text = "Fares are calculated dynamically based on distance, estimated journey duration, dynamic demand multipliers, and government taxes. No hidden charges will be added."
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
            setLineSpacing(4f, 1f)
        }

        container.addView(titleView)
        container.addView(descView)
        addView(container)
    }
}
