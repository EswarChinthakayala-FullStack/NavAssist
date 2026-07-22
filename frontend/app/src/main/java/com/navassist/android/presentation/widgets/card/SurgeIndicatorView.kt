package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class SurgeIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val multiplierView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#2A2114"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#F59E0B")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density

        val iconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(R.drawable.ic_feature_tracking)
            setColorFilter(Color.parseColor("#F59E0B"))
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * density).toInt()
            }
        }

        val titleView = TextView(context).apply {
            text = "⚡ Surge Pricing Active"
            setTextColor(Color.parseColor("#F59E0B"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val descView = TextView(context).apply {
            text = "High demand in your pickup zone has temporarily adjusted fares."
            setTextColor(Color.parseColor("#D4D4D8"))
            textSize = 12f
        }

        textLayout.addView(titleView)
        textLayout.addView(descView)

        multiplierView = TextView(context).apply {
            text = "1.5x"
            setTextColor(Color.parseColor("#F59E0B"))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        container.addView(iconView)
        container.addView(textLayout)
        container.addView(multiplierView)
        addView(container)
    }

    fun setMultiplier(multiplier: Double) {
        multiplierView.text = "${multiplier}x"
        visibility = if (multiplier > 1.0) VISIBLE else GONE
    }
}
