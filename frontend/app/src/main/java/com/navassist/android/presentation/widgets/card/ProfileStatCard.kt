package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class ProfileStatCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val valueView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 4f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val paddingPx = (14 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        valueView = TextView(context).apply {
            text = "4.9 ★"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        titleView = TextView(context).apply {
            text = "Rating"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        container.addView(valueView)
        container.addView(titleView)
        addView(container)
    }

    fun setStat(value: String, title: String) {
        valueView.text = value
        titleView.text = title
    }
}
