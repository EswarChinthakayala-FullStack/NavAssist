package com.navassist.android.presentation.assistant.home.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R
import com.navassist.android.domain.model.Booking

class BookingRequestCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val tvGuestName: TextView
    val tvRating: TextView
    val tvFare: TextView
    val tvPickup: TextView
    val tvDestination: TextView
    val btnAccept: MaterialButton
    val btnDecline: MaterialButton

    var onAcceptClickListener: (() -> Unit)? = null
    var onDeclineClickListener: (() -> Unit)? = null

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

        // Top Row: Guest & Fare
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val ivAvatar = ImageView(context).apply {
            val size = (44 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (12 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_passenger)
        }

        val nameCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvGuestName = TextView(context).apply {
            text = "Passenger Name"
            textSize = 16f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvRating = TextView(context).apply {
            text = "★ 4.9 • Cash Payment"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        nameCol.addView(tvGuestName)
        nameCol.addView(tvRating)

        tvFare = TextView(context).apply {
            text = "₹250"
            textSize = 22f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        topRow.addView(ivAvatar)
        topRow.addView(nameCol)
        topRow.addView(tvFare)

        // Route details
        val routeLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, (14 * density).toInt(), 0, (14 * density).toInt())
        }

        tvPickup = TextView(context).apply {
            text = "📍 Pickup: 123 Main St, Tech Park"
            textSize = 13f
            setTextColor(Color.parseColor("#D4D4D8"))
            maxLines = 1
        }

        tvDestination = TextView(context).apply {
            text = "🏁 Destination: Central Railway Station"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            maxLines = 1
            setPadding(0, (6 * density).toInt(), 0, 0)
        }

        routeLayout.addView(tvPickup)
        routeLayout.addView(tvDestination)

        // Action Buttons Row
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        btnDecline = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(0, (48 * density).toInt(), 1f).apply {
                marginEnd = (8 * density).toInt()
            }
            text = "Decline"
            setTextColor(Color.parseColor("#EF4444"))
            setBackgroundColor(Color.parseColor("#26EF4444"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
        }

        btnAccept = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, (48 * density).toInt(), 1f).apply {
                marginStart = (8 * density).toInt()
            }
            text = "Accept Request"
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnDecline.setOnClickListener { onDeclineClickListener?.invoke() }
        btnAccept.setOnClickListener { onAcceptClickListener?.invoke() }

        actionsRow.addView(btnDecline)
        actionsRow.addView(btnAccept)

        rootLayout.addView(topRow)
        rootLayout.addView(routeLayout)
        rootLayout.addView(actionsRow)

        addView(rootLayout)
    }

    fun bindBooking(booking: Booking) {
        tvGuestName.text = booking.guestName
        tvRating.text = "★ 4.9 • Digital Pay"
        tvFare.text = "₹${booking.fare.toInt()}"
        tvPickup.text = "📍 Pickup: ${booking.pickupLocation.address}"
        tvDestination.text = "🏁 Destination: ${booking.destinationLocation.address}"
    }
}
