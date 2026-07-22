package com.navassist.android.presentation.admin.bookings.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.data.remote.api.AdminBookingDto

class AdminBookingCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvBookingCode: TextView
    private val tvRoute: TextView
    private val tvMeta: TextView
    private val tvFare: TextView
    private val badgeStatus: TextView

    init {
        radius = (18 * context.resources.displayMetrics.density)
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

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val col1 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvBookingCode = TextView(context).apply {
            text = "BK-10029"
            textSize = 16f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvMeta = TextView(context).apply {
            text = "Passenger #10 • Assistant #5 • 22 Jul 2026"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        col1.addView(tvBookingCode)
        col1.addView(tvMeta)

        badgeStatus = TextView(context).apply {
            text = "ACCEPTED"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        topRow.addView(col1)
        topRow.addView(badgeStatus)

        val divider = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                topMargin = (10 * density).toInt()
                bottomMargin = (10 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        val botRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        tvRoute = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Delhi Airport ➔ Railway Station"
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
        }

        tvFare = TextView(context).apply {
            text = "₹350.00"
            textSize = 15f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        botRow.addView(tvRoute)
        botRow.addView(tvFare)

        rootLayout.addView(topRow)
        rootLayout.addView(divider)
        rootLayout.addView(botRow)

        addView(rootLayout)
    }

    fun bindBooking(booking: AdminBookingDto) {
        tvBookingCode.text = booking.bookingCode
        tvMeta.text = "Passenger #${booking.guestId} • Assistant #${booking.assistantId ?: "Unassigned"}"
        tvRoute.text = "${booking.pickupAddress.take(20)}... ➔ ${booking.destinationAddress.take(20)}..."

        badgeStatus.text = booking.status.uppercase()
        when (booking.status.uppercase()) {
            "ACCEPTED", "COMPLETED" -> {
                badgeStatus.setTextColor(Color.parseColor("#22C55E"))
                badgeStatus.setBackgroundColor(Color.parseColor("#2622C55E"))
            }
            "SEARCHING", "PENDING" -> {
                badgeStatus.setTextColor(Color.parseColor("#F59E0B"))
                badgeStatus.setBackgroundColor(Color.parseColor("#26F59E0B"))
            }
            "CANCELLED" -> {
                badgeStatus.setTextColor(Color.parseColor("#EF4444"))
                badgeStatus.setBackgroundColor(Color.parseColor("#26EF4444"))
            }
            else -> {
                badgeStatus.setTextColor(Color.parseColor("#3B82F6"))
                badgeStatus.setBackgroundColor(Color.parseColor("#263B82F6"))
            }
        }
    }
}
