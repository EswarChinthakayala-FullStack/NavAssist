package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class DistanceEtaView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val infoTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        infoTextView = TextView(context).apply {
            text = "📍 1.8 km · 🕒 4 min away"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        addView(infoTextView)
    }

    fun setDistanceEta(distanceKm: Double, etaMins: Int) {
        val formattedDist = String.format("%.1f", distanceKm)
        infoTextView.text = "📍 $formattedDist km · 🕒 $etaMins min away"
    }
}
