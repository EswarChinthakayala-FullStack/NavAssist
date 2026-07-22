package com.navassist.android.presentation.assistant.booking.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class PaymentMethodCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvMethodName: TextView
    private val tvStatusBadge: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvIcon = TextView(context).apply {
            text = "💳 "
            textSize = 20f
            setPadding(0, 0, (10 * density).toInt(), 0)
        }

        val infoCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvHeader = TextView(context).apply {
            text = "PAYMENT METHOD"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvMethodName = TextView(context).apply {
            text = "Online UPI / Wallet"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        infoCol.addView(tvHeader)
        infoCol.addView(tvMethodName)

        tvStatusBadge = TextView(context).apply {
            text = "PAID ONLINE ✓"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (4 * density).toInt()
            val pH = (10 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        rootLayout.addView(tvIcon)
        rootLayout.addView(infoCol)
        rootLayout.addView(tvStatusBadge)

        addView(rootLayout)
    }

    fun setPaymentMethod(method: String = "Online UPI", isPrepaid: Boolean = true) {
        tvMethodName.text = method
        if (isPrepaid) {
            tvStatusBadge.text = "PREPAID ✓"
            tvStatusBadge.setTextColor(Color.parseColor("#22C55E"))
        } else {
            tvStatusBadge.text = "CASH ON ARRIVAL"
            tvStatusBadge.setTextColor(Color.parseColor("#F59E0B"))
        }
    }
}
