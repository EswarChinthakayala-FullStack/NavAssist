package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.price.AnimatedPriceView

class TotalFareCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val priceView: AnimatedPriceView
    private val subtitleView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 30f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val paddingPx = (24 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val labelView = TextView(context).apply {
            text = "Total Estimated Fare"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
        }

        priceView = AnimatedPriceView(context)

        val density = context.resources.displayMetrics.density
        subtitleView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (6 * density).toInt()
            }
            text = "Est. Distance: 8.4 km · Est. Time: 18 mins"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 12f
        }

        container.addView(labelView)
        container.addView(priceView)
        container.addView(subtitleView)
        addView(container)
    }

    fun setFare(total: Double, distKm: Double = 8.4, etaMins: Int = 18) {
        priceView.animatePrice(total)
        val formattedDist = String.format("%.1f", distKm)
        subtitleView.text = "Est. Distance: $formattedDist km · Est. Time: $etaMins mins"
    }
}
