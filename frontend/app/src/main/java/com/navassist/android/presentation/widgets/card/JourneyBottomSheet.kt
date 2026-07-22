package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.badge.StatusBadgeView

class JourneyBottomSheet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val statusBadge: StatusBadgeView
    private val titleView: TextView
    private val subtitleView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 28f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 12f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val handle = android.view.View(context).apply {
            val density = context.resources.displayMetrics.density
            layoutParams = LinearLayout.LayoutParams((36 * density).toInt(), (4 * density).toInt()).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                bottomMargin = (12 * density).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#3F3F46"))
                cornerRadius = 2f * density
            }
        }

        statusBadge = StatusBadgeView(context).apply {
            setStatus("ON THE WAY")
        }

        val density = context.resources.displayMetrics.density

        titleView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
            text = "Vikram is on the way to meet you"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 17f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        subtitleView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (4 * density).toInt()
            }
            text = "Estimated arrival distance: 2.4 km · Current speed: 28 km/h"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        container.addView(handle)
        container.addView(statusBadge)
        container.addView(titleView)
        container.addView(subtitleView)

        addView(container)
    }

    fun updateProgress(status: String, distanceKm: Double, speedKmH: Int) {
        statusBadge.setStatus(status)
        val formattedDist = String.format("%.1f", distanceKm)
        subtitleView.text = "Estimated arrival distance: $formattedDist km · Current speed: $speedKmH km/h"
    }
}
