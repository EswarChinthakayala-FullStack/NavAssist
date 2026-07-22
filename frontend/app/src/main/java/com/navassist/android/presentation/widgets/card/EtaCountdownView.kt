package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class EtaCountdownView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val etaTextView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 26f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        etaTextView = TextView(context).apply {
            text = "8 min"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 32f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        val labelView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (2 * density).toInt()
            }
            text = "Estimated arrival time to your pickup location"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        container.addView(etaTextView)
        container.addView(labelView)
        addView(container)
    }

    fun setEtaMinutes(etaMins: Int) {
        etaTextView.text = "$etaMins min"
    }
}
