package com.navassist.android.presentation.widgets.input

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.TextView

/**
 * OtpBoxView represents a single 50dp x 60dp animated OTP digit box
 * supporting Empty, Focused, Filled, Error, and Success visual states.
 */
class OtpBoxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class State {
        EMPTY,
        FOCUSED,
        FILLED,
        ERROR,
        SUCCESS
    }

    private val tvDigit: TextView
    private val cursorView: OtpCursorView
    private val backgroundDrawable: GradientDrawable

    private var currentState: State = State.EMPTY
    private var popAnimator: AnimatorSet? = null

    init {
        clipChildren = false
        clipToPadding = false

        val density = context.resources.displayMetrics.density

        // Setup background drawable
        backgroundDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f * density
            setColor(Color.parseColor("#18181B"))
            setStroke((1.5f * density).toInt(), Color.parseColor("#2A2A2A"))
        }
        background = backgroundDrawable

        // Setup Digit TextView
        tvDigit = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            visibility = View.GONE
        }
        addView(tvDigit)

        // Setup Blinking Cursor
        cursorView = OtpCursorView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
        }
        addView(cursorView)
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val widthPx = (50 * density).toInt()
        val heightPx = (60 * density).toInt()
        setMeasuredDimension(
            resolveSize(widthPx, widthMeasureSpec),
            resolveSize(heightPx, heightMeasureSpec)
        )
    }

    fun setDigit(digit: Char?) {
        if (digit != null && digit.isDigit()) {
            tvDigit.text = digit.toString()
            tvDigit.visibility = View.VISIBLE
            cursorView.stopBlinking()

            if (currentState != State.FILLED) {
                setState(State.FILLED)
                animateDigitPop()
            }
        } else {
            tvDigit.text = ""
            tvDigit.visibility = View.GONE
        }
    }

    fun setState(state: State) {
        currentState = state
        val density = resources.displayMetrics.density
        val strokeWidthPx = (1.5f * density).toInt()

        when (state) {
            State.EMPTY -> {
                backgroundDrawable.setColor(Color.parseColor("#18181B"))
                backgroundDrawable.setStroke(strokeWidthPx, Color.parseColor("#2A2A2A"))
                cursorView.stopBlinking()
                animateScale(1.0f)
            }
            State.FOCUSED -> {
                backgroundDrawable.setColor(Color.parseColor("#242429"))
                backgroundDrawable.setStroke((2f * density).toInt(), Color.parseColor("#FFFFFF"))
                cursorView.startBlinking()
                animateScale(1.02f)
            }
            State.FILLED -> {
                backgroundDrawable.setColor(Color.parseColor("#202024"))
                backgroundDrawable.setStroke((1.5f * density).toInt(), Color.parseColor("#FFFFFF"))
                cursorView.stopBlinking()
                animateScale(1.0f)
            }
            State.ERROR -> {
                backgroundDrawable.setColor(Color.parseColor("#26181A"))
                backgroundDrawable.setStroke((2f * density).toInt(), Color.parseColor("#EF4444"))
                cursorView.stopBlinking()
                animateShake()
            }
            State.SUCCESS -> {
                backgroundDrawable.setColor(Color.parseColor("#18261E"))
                backgroundDrawable.setStroke((2f * density).toInt(), Color.parseColor("#22C55E"))
                cursorView.stopBlinking()
                animateSuccessPulse()
            }
        }
    }

    private fun animateDigitPop() {
        popAnimator?.cancel()
        val scaleX1 = ObjectAnimator.ofFloat(this, View.SCALE_X, 0.92f)
        val scaleY1 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0.92f)
        scaleX1.duration = 40
        scaleY1.duration = 40

        val scaleX2 = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.05f)
        val scaleY2 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.05f)
        scaleX2.duration = 50
        scaleY2.duration = 50
        scaleX2.interpolator = OvershootInterpolator(2.0f)

        val scaleX3 = ObjectAnimator.ofFloat(this, View.SCALE_X, 1.0f)
        val scaleY3 = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1.0f)
        scaleX3.duration = 30
        scaleY3.duration = 30

        popAnimator = AnimatorSet().apply {
            play(scaleX1).with(scaleY1)
            play(scaleX2).with(scaleY2).after(scaleX1)
            play(scaleX3).with(scaleY3).after(scaleX2)
            start()
        }
    }

    private fun animateScale(targetScale: Float) {
        animate()
            .scaleX(targetScale)
            .scaleY(targetScale)
            .setDuration(120)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun animateShake() {
        ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0f, 16f, -16f, 12f, -12f, 6f, -6f, 0f).apply {
            duration = 250
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateSuccessPulse() {
        animate()
            .scaleX(1.06f)
            .scaleY(1.06f)
            .setDuration(120)
            .withEndAction {
                animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(120)
                    .start()
            }
            .start()
    }
}
