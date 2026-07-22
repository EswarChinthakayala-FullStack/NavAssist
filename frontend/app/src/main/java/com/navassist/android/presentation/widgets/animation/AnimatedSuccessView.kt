package com.navassist.android.presentation.widgets.animation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class AnimatedSuccessView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22C55E")
        style = Paint.Style.FILL
    }

    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#09090B")
        style = Paint.Style.STROKE
        strokeWidth = 8f * context.resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    private var animScale: Float = 0f

    init {
        playSuccessAnimation()
    }

    fun playSuccessAnimation() {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1200
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { anim ->
                animScale = anim.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (width.coerceAtMost(height) / 2f - 12f) * animScale

        canvas.drawCircle(cx, cy, radius, circlePaint)

        if (animScale > 0.5f) {
            val checkProgress = ((animScale - 0.5f) / 0.5f).coerceIn(0f, 1f)
            val p1x = cx - radius * 0.3f
            val p1y = cy
            val p2x = cx - radius * 0.05f
            val p2y = cy + radius * 0.25f
            val p3x = cx + radius * 0.35f
            val p3y = cy - radius * 0.2f

            val path = android.graphics.Path()
            path.moveTo(p1x, p1y)
            path.lineTo(p2x, p2y)
            path.lineTo(p1x + (p3x - p1x) * checkProgress, p1y + (p3y - p1y) * checkProgress)
            canvas.drawPath(path, checkPaint)
        }
    }
}
