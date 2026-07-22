package com.navassist.android.presentation.assistant.earnings.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.domain.model.TripEarningsItem

class TripEarningCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val tvGuestName: TextView
    val tvDate: TextView
    val tvFare: TextView
    val tvNetEarnings: TextView
    val tvPickup: TextView
    val tvDestination: TextView
    val tvStatusBadge: TextView

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

        // Top Row: Guest & Earnings
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val nameCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvGuestName = TextView(context).apply {
            text = "Passenger Name"
            textSize = 15f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvDate = TextView(context).apply {
            text = "Today, 02:45 PM"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        nameCol.addView(tvGuestName)
        nameCol.addView(tvDate)

        val fareCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        tvNetEarnings = TextView(context).apply {
            text = "+₹235.00"
            textSize = 18f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvFare = TextView(context).apply {
            text = "Fare ₹250.00"
            textSize = 11f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        fareCol.addView(tvNetEarnings)
        fareCol.addView(tvFare)

        topRow.addView(nameCol)
        topRow.addView(fareCol)

        // Route details
        val routeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, (12 * density).toInt(), 0, (8 * density).toInt())
        }

        tvPickup = TextView(context).apply {
            text = "🟢 123 Main Street, Tech Hub"
            textSize = 12f
            setTextColor(Color.parseColor("#D4D4D8"))
            maxLines = 1
        }

        tvDestination = TextView(context).apply {
            text = "🔴 Central Railway Station"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            maxLines = 1
            setPadding(0, (4 * density).toInt(), 0, 0)
        }

        routeLayout.addView(tvPickup)
        routeLayout.addView(tvDestination)

        // Bottom status badge
        tvStatusBadge = TextView(context).apply {
            text = "COMPLETED ✓ • Online UPI"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        rootLayout.addView(topRow)
        rootLayout.addView(routeLayout)
        rootLayout.addView(tvStatusBadge)

        addView(rootLayout)
    }

    fun bindTrip(item: TripEarningsItem) {
        tvGuestName.text = item.guestName
        tvDate.text = item.date
        tvNetEarnings.text = "+₹%.2f".format(item.netEarnings)
        tvFare.text = "Gross ₹%.2f".format(item.fareAmount)
        tvPickup.text = "🟢 ${item.pickup}"
        tvDestination.text = "🔴 ${item.destination}"
        tvStatusBadge.text = "${item.status} ✓ • ${item.paymentMethod}"
    }
}
