package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView

class LoadingCouponCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 26f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 4f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density

        val titleSkeleton = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams((160 * density).toInt(), (18 * density).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#242428"))
                cornerRadius = 8f * density
            }
        }

        val descSkeleton = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams((220 * density).toInt(), (14 * density).toInt()).apply {
                topMargin = (10 * density).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#242428"))
                cornerRadius = 6f * density
            }
        }

        container.addView(titleSkeleton)
        container.addView(descSkeleton)
        addView(container)
    }
}
