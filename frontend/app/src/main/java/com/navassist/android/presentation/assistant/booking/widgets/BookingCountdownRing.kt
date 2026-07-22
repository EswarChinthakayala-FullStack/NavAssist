package com.navassist.android.presentation.assistant.booking.widgets

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.View

class BookingCountdownRing @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.parseColor("#27272A")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#22C55E")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 48f
        color = Color.parseColor("#FAFAFA")
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 24f
        color = Color.parseColor("#A1A1AA")
    }

    private val rectF = RectF()
    private var totalSeconds = 30
    private var remainingSeconds = 30
    private var progressSweep = 360f
    private var countDownTimer: CountDownTimer? = null
    private var pulseAnimator: ObjectAnimator? = null

    var onCountdownFinished: (() -> Unit)? = null
    var onTickListener: ((Int) -> Unit)? = null

    init {
        val density = context.resources.displayMetrics.density
        bgPaint.strokeWidth = 5f * density
        progressPaint.strokeWidth = 6f * density
        textPaint.textSize = 22f * density
        labelPaint.textSize = 10f * density
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val stroke = progressPaint.strokeWidth
        rectF.set(stroke, stroke, w.toFloat() - stroke, h.toFloat() - stroke)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawOval(rectF, bgPaint)

        val sweepAngle = (remainingSeconds.toFloat() / totalSeconds.toFloat()) * 360f
        canvas.drawArc(rectF, -90f, sweepAngle, false, progressPaint)

        val centerX = width / 2f
        val centerY = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f - 8f

        val formattedText = String.format("00:%02d", remainingSeconds)
        canvas.drawText(formattedText, centerX, centerY, textPaint)
        canvas.drawText("SEC REMAINING", centerX, centerY + (textPaint.textSize * 0.7f), labelPaint)
    }

    fun startCountdown(durationSeconds: Int = 30) {
        stopCountdown()
        totalSeconds = durationSeconds
        remainingSeconds = durationSeconds
        invalidate()

        countDownTimer = object : CountDownTimer((durationSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = (millisUntilFinished / 1000).toInt()
                updateColorAndAnimations(remainingSeconds)
                onTickListener?.invoke(remainingSeconds)
                invalidate()
            }

            override fun onFinish() {
                remainingSeconds = 0
                invalidate()
                stopPulse()
                onCountdownFinished?.invoke()
            }
        }.start()
    }

    fun stopCountdown() {
        countDownTimer?.cancel()
        countDownTimer = null
        stopPulse()
    }

    private fun updateColorAndAnimations(seconds: Int) {
        val newColor = when {
            seconds <= 3 -> Color.parseColor("#EF4444")
            seconds <= 5 -> Color.parseColor("#F59E0B")
            else -> Color.parseColor("#22C55E")
        }
        progressPaint.color = newColor

        if (seconds <= 3 && pulseAnimator == null) {
            startPulse()
            vibrateDevice()
        }
    }

    private fun startPulse() {
        pulseAnimator = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 1.05f, 1.0f).apply {
            duration = 400
            repeatCount = ValueAnimator.INFINITE
            start()
        }
        ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 1.05f, 1.0f).apply {
            duration = 400
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun stopPulse() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        scaleX = 1.0f
        scaleY = 1.0f
    }

    private fun vibrateDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(300)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopCountdown()
    }
}
