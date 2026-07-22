package com.navassist.android.presentation.admin.users.widgets

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
import com.navassist.android.data.remote.api.AdminUserDto

class AdminUserCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivAvatar: ImageView
    val tvName: TextView
    val tvEmailPhone: TextView
    val badgeRole: TextView
    val badgeStatus: TextView
    val btnSuspend: MaterialButton

    var onSuspendClickListener: (() -> Unit)? = null

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
            gravity = Gravity.CENTER_VERTICAL
        }

        ivAvatar = ImageView(context).apply {
            val size = (40 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (12 * density).toInt()
            }
            setImageResource(R.drawable.ic_person_outline)
        }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvName = TextView(context).apply {
            text = "User Full Name"
            textSize = 15f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvEmailPhone = TextView(context).apply {
            text = "+91 9876543210 • user@navassist.in"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvName)
        colText.addView(tvEmailPhone)

        val colBadges = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        badgeRole = TextView(context).apply {
            text = "PASSENGER"
            textSize = 10f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#27272A"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        badgeStatus = TextView(context).apply {
            text = "ACTIVE"
            textSize = 10f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        colBadges.addView(badgeRole)
        colBadges.addView(badgeStatus)

        topRow.addView(ivAvatar)
        topRow.addView(colText)
        topRow.addView(colBadges)

        btnSuspend = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (38 * density).toInt()
            ).apply {
                topMargin = (10 * density).toInt()
            }
            text = "Suspend / Toggle Status"
            textSize = 12f
            setTextColor(Color.parseColor("#EF4444"))
            setBackgroundColor(Color.parseColor("#1A0909"))
            cornerRadius = (10 * density).toInt()
            isAllCaps = false
        }

        btnSuspend.setOnClickListener { onSuspendClickListener?.invoke() }

        rootLayout.addView(topRow)
        rootLayout.addView(btnSuspend)

        addView(rootLayout)
    }

    fun bindUser(user: AdminUserDto) {
        tvName.text = user.fullName ?: "User #${user.id}"
        tvEmailPhone.text = "${user.phone}${if (user.email != null) " • ${user.email}" else ""}"

        badgeRole.text = user.role.uppercase()
        if (user.role.uppercase() == "ASSISTANT") {
            badgeRole.setTextColor(Color.parseColor("#F59E0B"))
            badgeRole.setBackgroundColor(Color.parseColor("#26F59E0B"))
        } else {
            badgeRole.setTextColor(Color.parseColor("#FAFAFA"))
            badgeRole.setBackgroundColor(Color.parseColor("#27272A"))
        }

        if (user.isActive) {
            badgeStatus.text = "ACTIVE"
            badgeStatus.setTextColor(Color.parseColor("#22C55E"))
            badgeStatus.setBackgroundColor(Color.parseColor("#2622C55E"))
            btnSuspend.text = "Suspend Account 🚫"
            btnSuspend.setTextColor(Color.parseColor("#EF4444"))
        } else {
            badgeStatus.text = "SUSPENDED"
            badgeStatus.setTextColor(Color.parseColor("#EF4444"))
            badgeStatus.setBackgroundColor(Color.parseColor("#26EF4444"))
            btnSuspend.text = "Reactivate Account ✓"
            btnSuspend.setTextColor(Color.parseColor("#22C55E"))
        }
    }
}
