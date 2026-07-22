package com.navassist.android.presentation.widgets.input

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * OtpCursorView represents a premium, smooth blinking vertical cursor indicator
 * positioned inside the currently active OTP input box.
 */
class OtpCursorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FAFAFA")
        style = Paint.Style.FILL
    }

    private val rectF = RectF()
    private var blinkAnimator: ValueAnimator? = null
    private var isBlinking = false

    init {
        visibility = GONE
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val widthPx = (2.5f * density).toInt()
        val heightPx = (24f * density).toInt()
        setMeasuredDimension(
            resolveSize(widthPx, widthMeasureSpec),
            resolveSize(heightPx, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cornerRadius = 1.25f * density
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, cursorPaint)
    }

    fun startBlinking() {
        if (isBlinking) return
        isBlinking = true
        visibility = VISIBLE
        alpha = 1f

        blinkAnimator?.cancel()
        blinkAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = 500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                alpha = animator.animatedValue as Float
            }
            start()
        }
    }

    fun stopBlinking() {
        isBlinking = false
        blinkAnimator?.cancel()
        blinkAnimator = null
        alpha = 0f
        visibility = GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopBlinking()
    }
}
