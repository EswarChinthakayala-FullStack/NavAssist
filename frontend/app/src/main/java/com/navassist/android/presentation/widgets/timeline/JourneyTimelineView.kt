package com.navassist.android.presentation.widgets.timeline

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class JourneyTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 26f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val titleView = TextView(context).apply {
            text = "Journey Progress"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density

        val step1 = createStepView("✓ Booking Confirmed & Verified", isCompleted = true)
        val step2 = createStepView("✓ Travel Assistant Assigned", isCompleted = true)
        val step3 = createStepView("● Assistant Preparing to Depart", isCurrent = true)
        val step4 = createStepView("○ On The Way to Pickup Point")
        val step5 = createStepView("○ Reached Pickup & Meet")

        container.addView(titleView)
        container.addView(step1)
        container.addView(step2)
        container.addView(step3)
        container.addView(step4)
        container.addView(step5)

        addView(container)
    }

    private fun createStepView(title: String, isCompleted: Boolean = false, isCurrent: Boolean = false): TextView {
        val density = context.resources.displayMetrics.density
        return TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (10 * density).toInt()
            }
            text = title
            textSize = 13f
            when {
                isCompleted -> setTextColor(Color.parseColor("#22C55E"))
                isCurrent -> {
                    setTextColor(Color.parseColor("#FAFAFA"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                else -> setTextColor(Color.parseColor("#71717A"))
            }
        }
    }
}
