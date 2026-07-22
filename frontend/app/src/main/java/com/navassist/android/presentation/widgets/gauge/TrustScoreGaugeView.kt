package com.navassist.android.presentation.widgets.gauge

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class TrustScoreGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#27272A")
        style = Paint.Style.STROKE
        strokeWidth = 14f * context.resources.displayMetrics.density
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.STROKE
        strokeWidth = 14f * context.resources.displayMetrics.density
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FAFAFA")
        textSize = 28f * context.resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#A1A1AA")
        textSize = 12f * context.resources.displayMetrics.density
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()
    private var scorePercent: Int = 96

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = 20f * context.resources.displayMetrics.density
        rectF.set(padding, padding, w - padding, h - padding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background Ring
        canvas.drawArc(rectF, 135f, 270f, false, backgroundPaint)

        // Progress Arc
        val sweepAngle = (scorePercent / 100f) * 270f
        canvas.drawArc(rectF, 135f, sweepAngle, false, progressPaint)

        // Score Text
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawText("$scorePercent%", centerX, centerY + (8f * context.resources.displayMetrics.density), textPaint)
        canvas.drawText("TRUST SCORE", centerX, centerY + (28f * context.resources.displayMetrics.density), labelPaint)
    }

    fun setScore(score: Int) {
        this.scorePercent = score.coerceIn(0, 100)
        invalidate()
    }
}
