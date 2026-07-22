package com.navassist.android.presentation.widgets.price

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class AnimatedPriceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var currentAmount: Double = 0.0

    init {
        setTextColor(Color.parseColor("#FAFAFA"))
        textSize = 36f
        setTypeface(null, android.graphics.Typeface.BOLD)
        text = "₹ 0"
    }

    fun animatePrice(targetAmount: Double, durationMillis: Long = 900) {
        val animator = ValueAnimator.ofFloat(0f, targetAmount.toFloat()).apply {
            duration = durationMillis
            addUpdateListener { anim ->
                val value = anim.animatedValue as Float
                text = "₹ ${value.toInt()}"
            }
        }
        animator.start()
        currentAmount = targetAmount
    }
}
