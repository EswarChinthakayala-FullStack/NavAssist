package com.navassist.android.presentation.assistant.earnings.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class IncomeTrendChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#22C55E")
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FAFAFA")
    }

    private val path = Path()
    private val dataPoints = listOf(1200f, 1800f, 1500f, 2200f, 2900f, 2400f, 3100f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val maxVal = dataPoints.maxOrNull() ?: 1f
        val stepX = w / (dataPoints.size - 1)

        path.reset()
        for (i in dataPoints.indices) {
            val x = i * stepX
            val y = h - (dataPoints[i] / maxVal) * (h * 0.7f) - (h * 0.15f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        canvas.drawPath(path, linePaint)

        for (i in dataPoints.indices) {
            val x = i * stepX
            val y = h - (dataPoints[i] / maxVal) * (h * 0.7f) - (h * 0.15f)
            canvas.drawCircle(x, y, 8f, dotPaint)
        }
    }
}
