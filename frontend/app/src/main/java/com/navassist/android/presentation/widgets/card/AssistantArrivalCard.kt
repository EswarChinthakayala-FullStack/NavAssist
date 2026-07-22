package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil3.load
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.badge.VerifiedBadgeView

class AssistantArrivalCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val avatarImageView: ImageView
    private val nameTextView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 28f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density
        avatarImageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((64 * density).toInt(), (64 * density).toInt())
            scaleType = ImageView.ScaleType.CENTER_CROP
            val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#27272A"))
                cornerRadius = 32f * density
            }
            background = backgroundDrawable
            clipToOutline = true
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (16 * density).toInt()
            }
        }

        val nameRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        nameTextView = TextView(context).apply {
            text = "Vikram Sharma"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 17f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val badge = VerifiedBadgeView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = (8 * density).toInt()
            }
        }

        nameRow.addView(nameTextView)
        nameRow.addView(badge)

        val descView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (4 * density).toInt()
            }
            text = "Arrived at Pickup Zone · Waiting for OTP verification"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        textLayout.addView(nameRow)
        textLayout.addView(descView)

        container.addView(avatarImageView)
        container.addView(textLayout)
        addView(container)

        setAssistantInfo("Vikram Sharma", "https://images.unsplash.com/photo-1534528741775-53994a69daeb")
    }

    fun setAssistantInfo(name: String, avatarUrl: String) {
        nameTextView.text = name
        if (avatarUrl.isNotBlank()) {
            avatarImageView.load(avatarUrl)
        }
    }
}
