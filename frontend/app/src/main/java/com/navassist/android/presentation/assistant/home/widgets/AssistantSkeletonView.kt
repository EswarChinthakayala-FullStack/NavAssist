package com.navassist.android.presentation.assistant.home.widgets

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

class AssistantSkeletonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val animator: ObjectAnimator

    init {
        orientation = VERTICAL
        val density = context.resources.displayMetrics.density
        val pad = (16 * density).toInt()
        setPadding(pad, pad, pad, pad)

        // Block 1: Header / Status Card Skeleton
        val card1 = createSkeletonBlock(120 * density)
        
        // Block 2: Stats Grid Skeleton
        val card2 = createSkeletonBlock(140 * density)

        // Block 3: Earnings Card Skeleton
        val card3 = createSkeletonBlock(160 * density)

        addView(card1)
        addView(card2)
        addView(card3)

        animator = ObjectAnimator.ofFloat(this, View.ALPHA, 0.3f, 0.7f, 0.3f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun createSkeletonBlock(heightPx: Float): View {
        val density = context.resources.displayMetrics.density
        return View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, heightPx.toInt()).apply {
                bottomMargin = (14 * density).toInt()
            }
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 18 * density
                setColor(Color.parseColor("#18181B"))
            }
            background = drawable
        }
    }

    fun stopShimmer() {
        animator.cancel()
        visibility = GONE
    }
}
