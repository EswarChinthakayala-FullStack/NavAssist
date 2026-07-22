package com.navassist.android.presentation.assistant.home.widgets

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.navassist.android.R

class EmptyStateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val ivIcon: ImageView
    private val tvHeading: TextView
    private val tvSubtitle: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val pad = (32 * density).toInt()
        setPadding(pad, pad, pad, pad)

        ivIcon = ImageView(context).apply {
            val size = (72 * density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                bottomMargin = (16 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_assistant)
            setColorFilter(Color.parseColor("#3F3F46"))
        }

        tvHeading = TextView(context).apply {
            text = "You're Offline"
            textSize = 20f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        tvSubtitle = TextView(context).apply {
            text = "Turn on availability above to start receiving live passenger booking requests nearby."
            textSize = 14f
            setTextColor(Color.parseColor("#71717A"))
            gravity = Gravity.CENTER
            setPadding(0, (8 * density).toInt(), 0, 0)
        }

        addView(ivIcon)
        addView(tvHeading)
        addView(tvSubtitle)

        // Float / pulse icon
        ObjectAnimator.ofFloat(ivIcon, "translationY", 0f, -10f, 0f).apply {
            duration = 2400
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }
}
