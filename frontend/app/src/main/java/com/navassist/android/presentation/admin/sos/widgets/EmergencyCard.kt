package com.navassist.android.presentation.admin.sos.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.navassist.android.data.remote.dto.sos.SosResponseDto

class EmergencyCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvIncidentTitle: TextView
    private val tvCoordinates: TextView
    private val tvMeta: TextView
    val btnResolve: MaterialButton

    var onResolveClickListener: (() -> Unit)? = null

    init {
        radius = (18 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#EF4444")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val topRow = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvIncidentTitle = TextView(context).apply {
            text = "SOS INCIDENT #1001 🔴"
            textSize = 15f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvMeta = TextView(context).apply {
            text = "User ID: #10 • Booking: #25 • Triggered: 2 mins ago"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvIncidentTitle)
        colText.addView(tvMeta)

        topRow.addView(colText)

        tvCoordinates = TextView(context).apply {
            text = "GPS: Lat 28.6139, Lng 77.2090 (Delhi)"
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
            setPadding(0, (8 * density).toInt(), 0, (12 * density).toInt())
        }

        btnResolve = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (42 * density).toInt()
            )
            text = "Resolve SOS Incident ✓"
            textSize = 13f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnResolve.setOnClickListener { onResolveClickListener?.invoke() }

        rootLayout.addView(topRow)
        rootLayout.addView(tvCoordinates)
        rootLayout.addView(btnResolve)

        addView(rootLayout)
    }

    fun bindAlert(alert: SosResponseDto) {
        tvIncidentTitle.text = "EMERGENCY SOS #${alert.id} 🔴"
        tvMeta.text = "User ID: #${alert.userId} • Booking ID: #${alert.bookingId ?: "N/A"}"
        tvCoordinates.text = "Live Coordinates: Lat %.4f, Lng %.4f".format(alert.latitude, alert.longitude)
    }
}
