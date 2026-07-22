package com.navassist.android.presentation.wallet.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.TextView

class TransactionStatusChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    init {
        textSize = 11f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        val density = context.resources.displayMetrics.density
        val pV = (3 * density).toInt()
        val pH = (8 * density).toInt()
        setPadding(pH, pV, pH, pV)
        setStatus("SUCCESS")
    }

    fun setStatus(statusStr: String) {
        text = statusStr.uppercase()
        when (statusStr.uppercase()) {
            "SUCCESS", "COMPLETED" -> {
                setTextColor(Color.parseColor("#22C55E"))
                setBackgroundColor(Color.parseColor("#2622C55E"))
            }
            "PENDING", "PROCESSING" -> {
                setTextColor(Color.parseColor("#F59E0B"))
                setBackgroundColor(Color.parseColor("#26F59E0B"))
            }
            "FAILED" -> {
                setTextColor(Color.parseColor("#EF4444"))
                setBackgroundColor(Color.parseColor("#26EF4444"))
            }
            "REFUNDED" -> {
                setTextColor(Color.parseColor("#3B82F6"))
                setBackgroundColor(Color.parseColor("#263B82F6"))
            }
            else -> {
                setTextColor(Color.parseColor("#71717A"))
                setBackgroundColor(Color.parseColor("#27272A"))
            }
        }
    }
}
