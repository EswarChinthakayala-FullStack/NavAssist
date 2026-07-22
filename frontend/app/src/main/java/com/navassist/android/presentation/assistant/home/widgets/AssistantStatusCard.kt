package com.navassist.android.presentation.assistant.home.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R
import com.navassist.android.domain.model.AssistantProfileData

class AssistantStatusCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivAvatar: ImageView
    val tvName: TextView
    val tvRating: TextView
    val badgeView: VerificationBadgeView
    val statusSwitch: OnlineStatusSwitch

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        // Top Info Row
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        ivAvatar = ImageView(context).apply {
            val size = (52 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (16 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_assistant)
        }

        val nameCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvName = TextView(context).apply {
            text = "NavAssist Guide"
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val metaRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (4 * density).toInt(), 0, 0)
        }

        tvRating = TextView(context).apply {
            text = "★ 4.9 (120 trips)"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        metaRow.addView(tvRating)
        nameCol.addView(tvName)
        nameCol.addView(metaRow)

        badgeView = VerificationBadgeView(context)

        topRow.addView(ivAvatar)
        topRow.addView(nameCol)
        topRow.addView(badgeView)

        // Divider
        val divider = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (1 * density).toInt()
            ).apply {
                topMargin = (16 * density).toInt()
                bottomMargin = (16 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        // Bottom Switch
        statusSwitch = OnlineStatusSwitch(context)

        rootLayout.addView(topRow)
        rootLayout.addView(divider)
        rootLayout.addView(statusSwitch)

        addView(rootLayout)
    }

    fun bindProfile(profile: AssistantProfileData) {
        tvName.text = profile.name
        tvRating.text = "★ ${String.format("%.1f", profile.rating)} (${profile.totalTrips} trips)"
        badgeView.setVerified(profile.verificationStatus.equals("VERIFIED", ignoreCase = true), profile.verificationStatus)
        statusSwitch.setOnlineState(profile.isOnline)
    }
}
