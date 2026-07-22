package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.input.OtpInputView

class OtpVerificationCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val otpInputView: OtpInputView
    private val timerTextView: TextView
    private val resendButton: MaterialButton

    var onVerifySubmit: ((String) -> Unit)? = null
    var onResendRequested: (() -> Unit)? = null

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 28f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val titleView = TextView(context).apply {
            text = "Enter Pickup OTP"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density

        otpInputView = OtpInputView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (16 * density).toInt()
                bottomMargin = (16 * density).toInt()
            }
            onOtpCompletedListener = { code ->
                onVerifySubmit?.invoke(code)
            }
        }

        timerTextView = TextView(context).apply {
            text = "OTP expires in 04:59"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        resendButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (40 * density).toInt()).apply {
                topMargin = (12 * density).toInt()
            }
            text = "Resend OTP"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 12f
            setCardBackgroundColor(Color.parseColor("#27272A"))
            cornerRadius = (14 * density).toInt()
            setOnClickListener { onResendRequested?.invoke() }
        }

        container.addView(titleView)
        container.addView(otpInputView)
        container.addView(timerTextView)
        container.addView(resendButton)

        addView(container)
    }

    fun setTimerText(text: String) {
        timerTextView.text = text
    }

    fun setErrorState() {
        otpInputView.setErrorState()
    }
}
