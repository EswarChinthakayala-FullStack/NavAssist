package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class CouponSuccessBanner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val savingsView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#16341E"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#22C55E")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        titleView = TextView(context).apply {
            text = "✓ Coupon Applied Successfully!"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        savingsView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (4 * density).toInt()
            }
            text = "You saved ₹50 on this trip"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
        }

        container.addView(titleView)
        container.addView(savingsView)
        addView(container)

        visibility = GONE
    }

    fun setSuccess(code: String, savingsAmount: Double) {
        titleView.text = "✓ Coupon '$code' Applied!"
        savingsView.text = "You saved ₹${savingsAmount.toInt()} on this journey"
        visibility = VISIBLE
    }
}
