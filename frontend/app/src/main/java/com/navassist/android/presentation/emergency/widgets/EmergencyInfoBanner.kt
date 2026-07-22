package com.navassist.android.presentation.emergency.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class EmergencyInfoBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#EF4444")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val ivShield = ImageView(context).apply {
            val size = (40 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (14 * density).toInt()
            }
            setImageResource(R.drawable.ic_feature_safety)
            setColorFilter(Color.parseColor("#EF4444"))
        }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvHeader = TextView(context).apply {
            text = "Your Safety Matters"
            textSize = 16f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(context).apply {
            text = "When Emergency SOS is triggered, these contacts instantly receive your live GPS tracking location and trip details."
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvHeader)
        colText.addView(tvSub)

        rootLayout.addView(ivShield)
        rootLayout.addView(colText)

        addView(rootLayout)
    }
}
