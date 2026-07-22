package com.navassist.android.presentation.widgets.badge

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class EtaBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val etaTextView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 20f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val paddingHorizontal = (16 * context.resources.displayMetrics.density).toInt()
            val paddingVertical = (10 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        }

        etaTextView = TextView(context).apply {
            text = "8 min"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val labelView = TextView(context).apply {
            text = "Est. Arrival"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 10f
        }

        container.addView(etaTextView)
        container.addView(labelView)
        addView(container)
    }

    fun setEtaMinutes(etaMins: Int) {
        etaTextView.text = "$etaMins min"
    }
}
