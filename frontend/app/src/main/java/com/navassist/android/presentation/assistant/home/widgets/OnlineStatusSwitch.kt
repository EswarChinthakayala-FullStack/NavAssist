package com.navassist.android.presentation.assistant.home.widgets

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch

class OnlineStatusSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val switchToggle: MaterialSwitch
    val tvStatusLabel: TextView
    private val vPulseGlow: View

    var onStatusChangeListener: ((Boolean) -> Unit)? = null
    private var pulseAnimator: ObjectAnimator? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density

        // Pulse Indicator Frame
        val pulseContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt()).apply {
                marginEnd = (12 * density).toInt()
            }
        }

        vPulseGlow = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.parseColor("#22C55E"))
            }
            background = drawable
            alpha = 0f
        }

        val vDot = View(context).apply {
            val size = (12 * density).toInt()
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.parseColor("#22C55E"))
            }
            background = drawable
        }

        pulseContainer.addView(vPulseGlow)
        pulseContainer.addView(vDot)

        tvStatusLabel = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            text = "YOU ARE OFFLINE"
            textSize = 14f
            setTextColor(Color.parseColor("#A1A1AA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        switchToggle = MaterialSwitch(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            contentDescription = "Toggle Online Availability"
        }

        addView(pulseContainer)
        addView(tvStatusLabel)
        addView(switchToggle)

        switchToggle.setOnCheckedChangeListener { _, isChecked ->
            updateUiState(isChecked)
            onStatusChangeListener?.invoke(isChecked)
        }
    }

    fun setOnlineState(isOnline: Boolean, notifyListener: Boolean = false) {
        if (!notifyListener) {
            switchToggle.setOnCheckedChangeListener(null)
        }
        switchToggle.isChecked = isOnline
        updateUiState(isOnline)
        if (!notifyListener) {
            switchToggle.setOnCheckedChangeListener { _, isChecked ->
                updateUiState(isChecked)
                onStatusChangeListener?.invoke(isChecked)
            }
        }
    }

    private fun updateUiState(isOnline: Boolean) {
        if (isOnline) {
            tvStatusLabel.text = "YOU ARE ONLINE"
            tvStatusLabel.setTextColor(Color.parseColor("#FAFAFA"))
            startPulseAnimation()
        } else {
            tvStatusLabel.text = "YOU ARE OFFLINE"
            tvStatusLabel.setTextColor(Color.parseColor("#71717A"))
            stopPulseAnimation()
        }
    }

    private fun startPulseAnimation() {
        stopPulseAnimation()
        pulseAnimator = ObjectAnimator.ofFloat(vPulseGlow, View.ALPHA, 0.2f, 0.8f, 0.2f).apply {
            duration = 1400
            repeatCount = ValueAnimator.INFINITE
            start()
        }
    }

    private fun stopPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        vPulseGlow.alpha = 0f
    }
}
