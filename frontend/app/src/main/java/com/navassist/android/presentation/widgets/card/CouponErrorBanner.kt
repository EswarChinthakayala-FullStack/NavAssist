package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class CouponErrorBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val errorTextView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#2A1414"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#EF4444")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        errorTextView = TextView(context).apply {
            text = "ⓘ Invalid coupon code. Please try another code."
            setTextColor(Color.parseColor("#EF4444"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        container.addView(errorTextView)
        addView(container)

        visibility = GONE
    }

    fun setError(errorMessage: String) {
        errorTextView.text = "ⓘ $errorMessage"
        visibility = VISIBLE
    }
}
