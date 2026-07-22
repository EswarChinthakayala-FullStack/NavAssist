package com.navassist.android.presentation.admin.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class AdminActionCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val ivIcon: ImageView
    private val tvTitle: TextView
    private val tvBadge: TextView

    var onActionClickListener: (() -> Unit)? = null

    init {
        radius = (16 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = (14 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        ivIcon = ImageView(context).apply {
            val size = (32 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (12 * density).toInt()
            }
            setColorFilter(Color.parseColor("#FAFAFA"))
        }

        tvTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Action Title"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvBadge = TextView(context).apply {
            text = "0 Pending"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        rootLayout.addView(ivIcon)
        rootLayout.addView(tvTitle)
        rootLayout.addView(tvBadge)

        setOnClickListener { onActionClickListener?.invoke() }
        addView(rootLayout)
    }

    fun bindAction(iconRes: Int, title: String, badgeText: String? = null, badgeColorHex: String = "#22C55E") {
        ivIcon.setImageResource(iconRes)
        tvTitle.text = title

        if (!badgeText.isNull_or_empty()) {
            tvBadge.visibility = VISIBLE
            tvBadge.text = badgeText
            tvBadge.setTextColor(Color.parseColor(badgeColorHex))
            tvBadge.setBackgroundColor(Color.parseColor("#26" + badgeColorHex.removePrefix("#")))
        } else {
            tvBadge.visibility = GONE
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
