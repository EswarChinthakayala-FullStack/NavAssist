package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class BookingSummaryMiniCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val pickupView: TextView
    private val destinationView: TextView
    private val scheduleTypeView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 20f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 4f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        pickupView = TextView(context).apply {
            text = "Pickup: --"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        destinationView = TextView(context).apply {
            text = "Destination: --"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        scheduleTypeView = TextView(context).apply {
            text = "Travel Mode: Ride Now"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        container.addView(pickupView)
        container.addView(destinationView)
        container.addView(scheduleTypeView)

        addView(container)
    }

    fun setSummary(pickup: String, destination: String, scheduleSummary: String) {
        pickupView.text = "Pickup: $pickup"
        destinationView.text = "Destination: $destination"
        scheduleTypeView.text = "Travel Mode: $scheduleSummary"
    }
}
