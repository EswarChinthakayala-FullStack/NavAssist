package com.navassist.android.presentation.widgets.background

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class NavigationGridBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#27272A")
        strokeWidth = 1f * context.resources.displayMetrics.density
        style = Paint.Style.STROKE
        alpha = (255 * 0.03f).toInt()
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3F3F46")
        style = Paint.Style.FILL
        alpha = (255 * 0.03f).toInt()
    }

    private val routePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#52525B")
        strokeWidth = 1.5f * context.resources.displayMetrics.density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        alpha = (255 * 0.02f).toInt()
    }

    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FAFAFA")
        style = Paint.Style.FILL
        alpha = (255 * 0.04f).toInt()
    }

    private val nodeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FAFAFA")
        strokeWidth = 1f * context.resources.displayMetrics.density
        style = Paint.Style.STROKE
        alpha = (255 * 0.04f).toInt()
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var animProgress = 0f
    private var floatAnimator: ValueAnimator? = null

    private val routePath1 = Path()
    private val routePath2 = Path()
    private val routePath3 = Path()

    init {
        setBackgroundColor(Color.parseColor("#09090B"))
        startAnimations()
    }

    private fun startAnimations() {
        floatAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                animProgress = anim.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildRoutePaths(w.toFloat(), h.toFloat())
    }

    private fun buildRoutePaths(w: Float, h: Float) {
        routePath1.reset()
        routePath1.moveTo(0f, h * 0.25f)
        routePath1.cubicTo(w * 0.35f, h * 0.15f, w * 0.65f, h * 0.4f, w, h * 0.3f)

        routePath2.reset()
        routePath2.moveTo(w * 0.1f, h)
        routePath2.cubicTo(w * 0.25f, h * 0.6f, w * 0.75f, h * 0.75f, w * 0.9f, 0f)

        routePath3.reset()
        routePath3.moveTo(0f, h * 0.85f)
        routePath3.cubicTo(w * 0.4f, h * 0.9f, w * 0.6f, h * 0.65f, w, h * 0.75f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthF = width.toFloat()
        val heightF = height.toFloat()
        val density = resources.displayMetrics.density
        val gridSizePx = 24f * density
        val dotRadius = 1.25f * density
        val nodeRadius = 3f * density

        // Layer 2 & 3: 24dp Square Grid & Intersection Dots
        var x = 0f
        while (x <= widthF) {
            canvas.drawLine(x, 0f, x, heightF, gridPaint)
            x += gridSizePx
        }

        var y = 0f
        while (y <= heightF) {
            canvas.drawLine(0f, y, widthF, y, gridPaint)
            y += gridSizePx
        }

        // Intersection Dots
        var dotX = 0f
        while (dotX <= widthF) {
            var dotY = 0f
            while (dotY <= heightF) {
                canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)
                dotY += gridSizePx
            }
            dotX += gridSizePx
        }

        // Layer 4: Abstract Navigation Routes
        canvas.drawPath(routePath1, routePaint)
        canvas.drawPath(routePath2, routePaint)
        canvas.drawPath(routePath3, routePaint)

        // Layer 5: Floating Navigation Nodes
        val floatOffset = Math.sin(animProgress * Math.PI * 2).toFloat() * (6f * density)
        val pulseRadius = nodeRadius + (Math.sin(animProgress * Math.PI * 4).toFloat() + 1f) * (1.5f * density)

        val node1X = (gridSizePx * 3)
        val node1Y = (gridSizePx * 6) + floatOffset
        canvas.drawCircle(node1X, node1Y, pulseRadius, nodePaint)
        canvas.drawCircle(node1X, node1Y, pulseRadius + (3f * density), nodeStrokePaint)

        val node2X = (gridSizePx * 10)
        val node2Y = (gridSizePx * 14) - floatOffset
        canvas.drawCircle(node2X, node2Y, nodeRadius, nodePaint)

        val node3X = (gridSizePx * 7)
        val node3Y = (gridSizePx * 20) + floatOffset
        canvas.drawCircle(node3X, node3Y, pulseRadius, nodePaint)

        // Layer 6 & 7: Radial Ambient Glow Overlays
        val glow1X = widthF * 0.25f + floatOffset * 2f
        val glow1Y = heightF * 0.15f
        val glow1Radius = Math.max(widthF, heightF) * 0.5f

        glowPaint.shader = RadialGradient(
            glow1X, glow1Y, glow1Radius,
            intArrayOf(Color.argb((255 * 0.05f).toInt(), 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(glow1X, glow1Y, glow1Radius, glowPaint)

        val glow2X = widthF * 0.8f - floatOffset * 2f
        val glow2Y = heightF * 0.85f
        val glow2Radius = Math.max(widthF, heightF) * 0.45f

        glowPaint.shader = RadialGradient(
            glow2X, glow2Y, glow2Radius,
            intArrayOf(Color.argb((255 * 0.04f).toInt(), 255, 255, 255), Color.TRANSPARENT),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(glow2X, glow2Y, glow2Radius, glowPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        floatAnimator?.cancel()
    }
}
