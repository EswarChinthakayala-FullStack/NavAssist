package com.navassist.android.presentation.assistant.booking.widgets

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

class PassengerCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivAvatar: ImageView
    val tvPassengerName: TextView
    val tvRatingTrips: TextView
    val btnChat: MaterialButton
    val btnCall: MaterialButton
    val btnProfile: MaterialButton

    var onChatClickListener: (() -> Unit)? = null
    var onCallClickListener: (() -> Unit)? = null
    var onProfileClickListener: (() -> Unit)? = null

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

        // Header Row
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        ivAvatar = ImageView(context).apply {
            val size = (52 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (14 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_passenger)
        }

        val nameCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvPassengerName = TextView(context).apply {
            text = "Passenger Name"
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvRatingTrips = TextView(context).apply {
            text = "★ 4.9 • 42 rides • Verified User"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        nameCol.addView(tvPassengerName)
        nameCol.addView(tvRatingTrips)

        topRow.addView(ivAvatar)
        topRow.addView(nameCol)

        // Action Buttons Row (Profile, Chat, Call)
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (14 * density).toInt(), 0, 0)
        }

        btnProfile = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(0, (40 * density).toInt(), 1f).apply {
                marginEnd = (6 * density).toInt()
            }
            text = "Profile"
            textSize = 12f
            setTextColor(Color.parseColor("#FAFAFA"))
            setBackgroundColor(Color.parseColor("#27272A"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
        }

        btnChat = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, (40 * density).toInt(), 1f).apply {
                marginStart = (6 * density).toInt()
                marginEnd = (6 * density).toInt()
            }
            text = "Chat"
            textSize = 12f
            setIconResource(R.drawable.ic_nav_chat)
            iconTint = android.content.res.ColorStateList.valueOf(Color.parseColor("#09090B"))
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnCall = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, (40 * density).toInt(), 1f).apply {
                marginStart = (6 * density).toInt()
            }
            text = "Call"
            textSize = 12f
            setIconResource(R.drawable.ic_phone_outline)
            iconTint = android.content.res.ColorStateList.valueOf(Color.parseColor("#FAFAFA"))
            setTextColor(Color.parseColor("#FAFAFA"))
            setBackgroundColor(Color.parseColor("#27272A"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
        }

        btnProfile.setOnClickListener { onProfileClickListener?.invoke() }
        btnChat.setOnClickListener { onChatClickListener?.invoke() }
        btnCall.setOnClickListener { onCallClickListener?.invoke() }

        actionsRow.addView(btnProfile)
        actionsRow.addView(btnChat)
        actionsRow.addView(btnCall)

        rootLayout.addView(topRow)
        rootLayout.addView(actionsRow)

        addView(rootLayout)
    }

    fun bindBooking(booking: Booking) {
        tvPassengerName.text = booking.guestName ?: "Passenger"
        tvRatingTrips.text = "★ 4.9 • 28 Rides • Verified"
    }
}
