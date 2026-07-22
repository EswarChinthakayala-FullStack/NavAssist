package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
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

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density

        // Pickup Row
        val pickupRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val pickupIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((18 * density).toInt(), (18 * density).toInt())
            setImageResource(R.drawable.ic_benefit_nearby)
        }

        pickupView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (10 * density).toInt()
            }
            text = "Select pickup location"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        pickupRow.addView(pickupIcon)
        pickupRow.addView(pickupView)

        // Connector Line
        val connectorLine = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams((2 * density).toInt(), (14 * density).toInt()).apply {
                marginStart = (8 * density).toInt()
                topMargin = (2 * density).toInt()
                bottomMargin = (2 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#303038"))
        }

        // Destination Row
        val destRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val destIcon = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((18 * density).toInt(), (18 * density).toInt())
            setImageResource(R.drawable.ic_benefit_pickup)
        }

        destinationView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (10 * density).toInt()
            }
            text = "Select destination"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        destRow.addView(destIcon)
        destRow.addView(destinationView)

        // Stats Footer Row
        val statsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
        }

        distanceView = TextView(context).apply {
            text = "Distance: -- km"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        etaView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = (16 * density).toInt()
            }
            text = "Est. Duration: -- mins"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        statsRow.addView(distanceView)
        statsRow.addView(etaView)

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
