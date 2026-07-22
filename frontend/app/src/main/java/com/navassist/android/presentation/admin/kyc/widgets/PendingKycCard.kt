package com.navassist.android.presentation.admin.kyc.widgets

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
import com.navassist.android.data.remote.api.AdminKycItemDto

class PendingKycCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivAvatar: ImageView
    val tvName: TextView
    val tvId: TextView
    val badgePriority: TextView
    val btnApprove: MaterialButton
    val btnReject: MaterialButton

    var onApproveClickListener: (() -> Unit)? = null
    var onRejectClickListener: (() -> Unit)? = null

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

        // Header Row
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        ivAvatar = ImageView(context).apply {
            val size = (44 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (12 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_assistant)
        }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvName = TextView(context).apply {
            text = "Assistant Applicant Name"
            textSize = 15f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvId = TextView(context).apply {
            text = "ID: #AST-1092 • Submitted Today"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvName)
        colText.addView(tvId)

        badgePriority = TextView(context).apply {
            text = "🔴 HIGH PRIORITY"
            textSize = 10f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#26EF4444"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        topRow.addView(ivAvatar)
        topRow.addView(colText)
        topRow.addView(badgePriority)

        // Actions Row
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (14 * density).toInt(), 0, 0)
        }

        btnReject = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(0, (42 * density).toInt(), 1f).apply {
                marginEnd = (6 * density).toInt()
            }
            text = "Reject ✕"
            textSize = 13f
            setTextColor(Color.parseColor("#EF4444"))
            setBackgroundColor(Color.parseColor("#1A0909"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
        }

        btnApprove = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, (42 * density).toInt(), 1f).apply {
                marginStart = (6 * density).toInt()
            }
            text = "Approve ✓"
            textSize = 13f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnReject.setOnClickListener { onRejectClickListener?.invoke() }
        btnApprove.setOnClickListener { onApproveClickListener?.invoke() }

        actionsRow.addView(btnReject)
        actionsRow.addView(btnApprove)

        rootLayout.addView(topRow)
        rootLayout.addView(actionsRow)

        addView(rootLayout)
    }

    fun bindItem(item: AdminKycItemDto) {
        tvName.text = item.fullName ?: "Assistant #${item.id}"
        tvId.text = "Assistant ID: #${item.id} • Status: ${item.verificationStatus}"

        if (item.id % 2 == 0) {
            badgePriority.text = "🔴 HIGH (Waiting > 2 days)"
            badgePriority.setTextColor(Color.parseColor("#EF4444"))
            badgePriority.setBackgroundColor(Color.parseColor("#26EF4444"))
        } else {
            badgePriority.text = "🟢 NEW (Waiting < 24h)"
            badgePriority.setTextColor(Color.parseColor("#22C55E"))
            badgePriority.setBackgroundColor(Color.parseColor("#2622C55E"))
        }
    }
}
