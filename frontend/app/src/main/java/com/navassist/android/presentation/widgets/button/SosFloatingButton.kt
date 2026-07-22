package com.navassist.android.presentation.widgets.button

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class SosFloatingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        radius = 26f * context.resources.displayMetrics.density
        cardElevation = 10f * context.resources.displayMetrics.density
        isClickable = true
        isFocusable = true

        val density = context.resources.displayMetrics.density
        layoutParams = android.view.ViewGroup.LayoutParams((52 * density).toInt(), (52 * density).toInt())

        val iconView = ImageView(context).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt(), Gravity.CENTER)
            setImageResource(R.drawable.ic_benefit_safety)
            setColorFilter(Color.parseColor("#EF4444"))
        }

        addView(iconView)
    }
}
