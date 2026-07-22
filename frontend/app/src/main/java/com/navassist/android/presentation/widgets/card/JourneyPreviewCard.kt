package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class JourneyPreviewCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val pickupView: TextView
    private val destinationView: TextView
    private val distanceView: TextView
    private val etaView: TextView
    private val routeChipView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#161616"))
        radius = 20f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 12f * context.resources.displayMetrics.density

        val density = context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        // Header Row: Route Type Badge
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = (12 * density).toInt()
            }
        }

        routeChipView = TextView(context).apply {
            text = "⚡ Fastest Driving Route • OSRM"
            setTextColor(Color.parseColor("#3B82F6"))
            textSize = 11f
            setTypeface(null, Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_current_location_pill)
            setPadding((10 * density).toInt(), (4 * density).toInt(), (10 * density).toInt(), (4 * density).toInt())
        }

        headerRow.addView(routeChipView)

        // Pickup Row
        val pickupRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val pickupIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt())
            setImageResource(R.drawable.ic_ms_location_on)
            setColorFilter(Color.parseColor("#22C55E"))
        }

        pickupView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (10 * density).toInt()
            }
            text = "Select pickup location"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }

        pickupRow.addView(pickupIcon)
        pickupRow.addView(pickupView)

        // Connector Line
        val connectorLine = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams((2 * density).toInt(), (16 * density).toInt()).apply {
                marginStart = (9 * density).toInt()
                topMargin = (3 * density).toInt()
                bottomMargin = (3 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#3F3F46"))
        }

        // Destination Row
        val destRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val destIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt())
            setImageResource(R.drawable.ic_ms_location_on)
            setColorFilter(Color.parseColor("#EF4444"))
        }

        destinationView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (10 * density).toInt()
            }
            text = "Select destination"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }

        destRow.addView(destIcon)
        destRow.addView(destinationView)

        // Stats Footer Row
        val statsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (14 * density).toInt()
            }
        }

        distanceView = TextView(context).apply {
            text = "Distance: -- km"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
        }

        etaView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = (16 * density).toInt()
            }
            text = "Est. Duration: -- mins"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
        }

        statsRow.addView(distanceView)
        statsRow.addView(etaView)

        container.addView(headerRow)
        container.addView(pickupRow)
        container.addView(connectorLine)
        container.addView(destRow)
        container.addView(statsRow)

        addView(container)
    }

    fun setJourney(pickupAddress: String, destinationAddress: String, distanceKm: Double, etaMins: Int) {
        pickupView.text = pickupAddress
        destinationView.text = destinationAddress
        distanceView.text = "Distance: ${String.format("%.1f", distanceKm)} km"
        etaView.text = "Est. Duration: $etaMins mins"
    }
}
