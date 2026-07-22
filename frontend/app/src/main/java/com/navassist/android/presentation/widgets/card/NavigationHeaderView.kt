package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class NavigationHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val addressView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 24f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        titleView = TextView(context).apply {
            text = "Navigating to Destination"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        addressView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (2 * density).toInt()
            }
            text = "International Airport, Terminal 2 Entrance"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        container.addView(titleView)
        container.addView(addressView)
        addView(container)
    }

    fun setDestination(address: String) {
        addressView.text = address
    }
}
