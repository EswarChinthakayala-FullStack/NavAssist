package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class SkeletonAssistantCard @JvmOverloads constructor(
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
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val avatarSkeleton = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams((64 * density).toInt(), (64 * density).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#242428"))
                cornerRadius = 32f * density
            }
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (14 * density).toInt()
            }
        }

        val nameSkeleton = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams((140 * density).toInt(), (16 * density).toInt())
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#242428"))
                cornerRadius = 8f * density
            }
        }

        val subSkeleton = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams((90 * density).toInt(), (12 * density).toInt()).apply {
                topMargin = (8 * density).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#242428"))
                cornerRadius = 6f * density
            }
        }

        textLayout.addView(nameSkeleton)
        textLayout.addView(subSkeleton)

        topRow.addView(avatarSkeleton)
        topRow.addView(textLayout)

        container.addView(topRow)
        addView(container)
    }
}
