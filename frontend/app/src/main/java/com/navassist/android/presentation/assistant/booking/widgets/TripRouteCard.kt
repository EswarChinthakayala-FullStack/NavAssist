package com.navassist.android.presentation.assistant.booking.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.domain.model.Booking

class TripRouteCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvPickup: TextView
    private val tvDestination: TextView
    private val tvDistance: TextView
    private val tvEta: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        // Header Title
        val tvHeader = TextView(context).apply {
            text = "TRIP ROUTE & NAVIGATION"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (12 * density).toInt())
        }

        // Pickup Row
        val pickupRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
        }
        val tvDotGreen = TextView(context).apply { text = "🟢 "; textSize = 14f }
        tvPickup = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Pickup: 123 Main Street"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        pickupRow.addView(tvDotGreen)
        pickupRow.addView(tvPickup)

        // Destination Row
        val destRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (6 * density).toInt(), 0, (12 * density).toInt())
        }
        val tvDotRed = TextView(context).apply { text = "🔴 "; textSize = 14f }
        tvDestination = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Destination: City Railway Station"
            textSize = 14f
            setTextColor(Color.parseColor("#D4D4D8"))
        }
        destRow.addView(tvDotRed)
        destRow.addView(tvDestination)

        // Divider
        val divider = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                bottomMargin = (12 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        // Stats Pill Row (Distance & ETA)
        val statsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        tvDistance = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "📏 Distance: 4.8 km"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        tvEta = TextView(context).apply {
            text = "⏱️ Est. Time: 15 mins"
            textSize = 13f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        statsRow.addView(tvDistance)
        statsRow.addView(tvEta)

        rootLayout.addView(tvHeader)
        rootLayout.addView(pickupRow)
        rootLayout.addView(destRow)
        rootLayout.addView(divider)
        rootLayout.addView(statsRow)

        addView(rootLayout)
    }

    fun bindBooking(booking: Booking) {
        tvPickup.text = "Pickup: ${booking.pickupLocation.address}"
        tvDestination.text = "Destination: ${booking.destinationLocation.address}"
        tvDistance.text = "📏 Distance: 5.2 km"
        tvEta.text = "⏱️ Est. Time: ${booking.estimatedMinutes} mins"
    }
}
