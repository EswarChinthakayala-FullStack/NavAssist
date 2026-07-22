package com.navassist.android.presentation.widgets.fare

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.navassist.android.R
import com.navassist.android.core.utils.CurrencyUtils

class FareBreakdownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tvTotalFare: TextView

    init {
        orientation = VERTICAL
        val density = context.resources.displayMetrics.density
        setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_surface))

        tvTotalFare = TextView(context).apply {
            textSize = 20f
            setTextColor(ContextCompat.getColor(context, R.color.color_primary))
            text = CurrencyUtils.formatInr(0.0)
        }
        addView(tvTotalFare)
    }

    fun setFareDetails(baseFare: Double, bookingFee: Double = 15.0, discount: Double = 0.0, totalFare: Double) {
        tvTotalFare.text = "Total Fare: ${CurrencyUtils.formatInr(totalFare)}"
    }
}
