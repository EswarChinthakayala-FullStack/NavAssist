package com.navassist.android.presentation.profile.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class ProfileHeaderCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivAvatar: ImageView
    val tvName: TextView
    val tvEmail: TextView
    val tvRoleBadge: TextView
    val tvTrustScore: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            val pad = (20 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        ivAvatar = ImageView(context).apply {
            val size = (80 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                bottomMargin = (12 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_passenger)
        }

        tvName = TextView(context).apply {
            text = "User Name"
            textSize = 20f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        tvEmail = TextView(context).apply {
            text = "user@navassist.app"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            gravity = Gravity.CENTER
            setPadding(0, (2 * density).toInt(), 0, (8 * density).toInt())
        }

        val badgeRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        tvRoleBadge = TextView(context).apply {
            text = "PASSENGER"
            textSize = 11f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#27272A"))
            val pV = (4 * density).toInt()
            val pH = (10 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        tvTrustScore = TextView(context).apply {
            text = "🛡️ Trust Score: 98%"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (4 * density).toInt()
            val pH = (10 * density).toInt()
            setPadding(pH, pV, pH, pV)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = (8 * density).toInt()
            }
        }

        badgeRow.addView(tvRoleBadge)
        badgeRow.addView(tvTrustScore)

        rootLayout.addView(ivAvatar)
        rootLayout.addView(tvName)
        rootLayout.addView(tvEmail)
        rootLayout.addView(badgeRow)

        addView(rootLayout)
    }

    fun bindHeader(name: String, email: String, role: String, isAssistant: Boolean, photoUrl: String? = null) {
        tvName.text = name
        tvEmail.text = email

        if (isAssistant) {
            tvRoleBadge.text = "VERIFIED ASSISTANT GUIDE"
            tvRoleBadge.setTextColor(Color.parseColor("#22C55E"))
            tvRoleBadge.setBackgroundColor(Color.parseColor("#2622C55E"))
            tvTrustScore.visibility = VISIBLE
            ivAvatar.setImageResource(R.drawable.ic_role_assistant)
        } else {
            tvRoleBadge.text = "PASSENGER ACCOUNT"
            tvRoleBadge.setTextColor(Color.parseColor("#FAFAFA"))
            tvRoleBadge.setBackgroundColor(Color.parseColor("#27272A"))
            tvTrustScore.visibility = GONE
            ivAvatar.setImageResource(R.drawable.ic_role_passenger)
        }
    }
}
