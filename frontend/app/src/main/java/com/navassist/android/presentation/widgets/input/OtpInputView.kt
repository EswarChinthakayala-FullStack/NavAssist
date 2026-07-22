package com.navassist.android.presentation.widgets.input

import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText

/**
 * OtpInputView implements the industry-standard Hidden EditText Architecture:
 * - One invisible EditText capturing all keyboard, paste, and auto-fill events
 * - Six animated OtpBoxViews visually displaying digits, focus cursor, and states
 */
class OtpInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val hiddenEditText: AppCompatEditText
    private val boxesContainer: LinearLayout
    private val boxViews = mutableListOf<OtpBoxView>()

    var onOtpCompletedListener: ((String) -> Unit)? = null
    var onOtpChangedListener: ((String) -> Unit)? = null

    private var isUpdatingText = false

    init {
        clipChildren = false
        clipToPadding = false

        val density = context.resources.displayMetrics.density

        // Horizontal container for 6 OTP boxes
        boxesContainer = LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
        }

        val boxMarginPx = (6 * density).toInt()

        for (i in 0 until 6) {
            val boxView = OtpBoxView(context).apply {
                layoutParams = LinearLayout.LayoutParams((50 * density).toInt(), (60 * density).toInt()).apply {
                    setMargins(boxMarginPx, 0, boxMarginPx, 0)
                }
            }
            boxViews.add(boxView)
            boxesContainer.addView(boxView)
        }

        // Invisible EditText overlaying the layout to receive input
        hiddenEditText = AppCompatEditText(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            alpha = 0.01f
            isFocusable = true
            isFocusableInTouchMode = true
            isCursorVisible = false
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            filters = arrayOf(InputFilter.LengthFilter(6))
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setAutofillHints("smsOTPCode")
            }
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        addView(boxesContainer)
        addView(hiddenEditText)

        setupListeners()
    }

    private fun setupListeners() {
        hiddenEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdatingText) return

                val rawText = s?.toString() ?: ""
                val cleanDigits = extractDigits(rawText)

                if (cleanDigits != rawText) {
                    isUpdatingText = true
                    hiddenEditText.setText(cleanDigits)
                    hiddenEditText.setSelection(cleanDigits.length)
                    isUpdatingText = false
                }

                updateBoxesState(cleanDigits)
                try {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                } catch (_: Exception) {}

                onOtpChangedListener?.invoke(cleanDigits)

                if (cleanDigits.length == 6) {
                    onOtpCompletedListener?.invoke(cleanDigits)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        hiddenEditText.setOnFocusChangeListener { _, hasFocus ->
            updateBoxesState(getOtp())
            if (hasFocus) {
                showKeyboard()
            }
        }

        setOnClickListener {
            requestFocusAndShowKeyboard()
        }

        boxesContainer.setOnClickListener {
            requestFocusAndShowKeyboard()
        }

        boxViews.forEach { box ->
            box.setOnClickListener {
                requestFocusAndShowKeyboard()
            }
        }
    }

    private fun extractDigits(input: String): String {
        val digits = input.filter { it.isDigit() }
        return if (digits.length > 6) digits.take(6) else digits
    }

    private fun updateBoxesState(otpText: String) {
        val hasFocus = hiddenEditText.hasFocus()
        val currentLength = otpText.length

        for (i in 0 until 6) {
            val boxView = boxViews[i]
            when {
                i < currentLength -> {
                    boxView.setDigit(otpText[i])
                }
                i == currentLength && hasFocus && currentLength < 6 -> {
                    boxView.setDigit(null)
                    boxView.setState(OtpBoxView.State.FOCUSED)
                }
                else -> {
                    boxView.setDigit(null)
                    boxView.setState(OtpBoxView.State.EMPTY)
                }
            }
        }
    }

    fun getOtp(): String {
        return hiddenEditText.text?.toString()?.filter { it.isDigit() } ?: ""
    }

    fun setOtp(otp: String) {
        val cleanDigits = extractDigits(otp)
        isUpdatingText = true
        hiddenEditText.setText(cleanDigits)
        hiddenEditText.setSelection(cleanDigits.length)
        isUpdatingText = false
        updateBoxesState(cleanDigits)

        onOtpChangedListener?.invoke(cleanDigits)
        if (cleanDigits.length == 6) {
            onOtpCompletedListener?.invoke(cleanDigits)
        }
    }

    fun setErrorState() {
        boxViews.forEach { it.setState(OtpBoxView.State.ERROR) }
        try {
            performHapticFeedback(HapticFeedbackConstants.REJECT)
        } catch (_: Exception) {}

        ObjectAnimator.ofFloat(boxesContainer, "translationX", 0f, 20f, -20f, 14f, -14f, 8f, -8f, 0f).apply {
            duration = 250
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun setSuccessState() {
        boxViews.forEach { it.setState(OtpBoxView.State.SUCCESS) }
    }

    fun resetState() {
        updateBoxesState(getOtp())
    }

    fun clear() {
        isUpdatingText = true
        hiddenEditText.setText("")
        isUpdatingText = false
        updateBoxesState("")
    }

    fun requestFocusAndShowKeyboard() {
        hiddenEditText.requestFocus()
        showKeyboard()
    }

    private fun showKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(hiddenEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
