package com.navassist.android.presentation.assistant.home.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.navassist.android.R

class VerificationBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val ivIcon: ImageView
    private val tvLabel: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density
        val padH = (10 * density).toInt()
        val padV = (4 * density).toInt()
        setPadding(padH, padV, padH, padV)
        setBackgroundResource(R.drawable.bg_ambient_glow)

        ivIcon = ImageView(context).apply {
            layoutParams = LayoutParams((14 * density).toInt(), (14 * density).toInt()).apply {
                marginEnd = (6 * density).toInt()
            }
            setImageResource(R.drawable.ic_benefit_safety)
            setColorFilter(Color.parseColor("#22C55E"))
        }

        tvLabel = TextView(context).apply {
            text = "VERIFIED GUIDE"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        addView(ivIcon)
        addView(tvLabel)
    }

    fun setVerified(isVerified: Boolean, statusText: String = "VERIFIED GUIDE") {
        tvLabel.text = statusText.uppercase()
        val colorStr = if (isVerified) "#22C55E" else "#F59E0B"
        val color = Color.parseColor(colorStr)
        tvLabel.setTextColor(color)
        ivIcon.setColorFilter(color)
    }
}
