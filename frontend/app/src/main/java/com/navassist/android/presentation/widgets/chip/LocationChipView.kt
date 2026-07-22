package com.navassist.android.presentation.widgets.chip

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.navassist.android.R

class LocationChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val titleView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (14 * density).toInt()
        val paddingVerticalPx = (10 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)
        setBackgroundResource(R.drawable.bg_input_field)

        iconView = ImageView(context).apply {
            layoutParams = LayoutParams((18 * density).toInt(), (18 * density).toInt())
            setImageResource(R.drawable.ic_benefit_pickup)
        }

        titleView = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = (8 * density).toInt()
            }
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(iconView)
        addView(titleView)

        isClickable = true
        isFocusable = true
    }

    fun setLocation(label: String, address: String) {
        titleView.text = label
        when (label.lowercase()) {
            "home" -> iconView.setImageResource(R.drawable.ic_person_outline)
            "office", "work" -> iconView.setImageResource(R.drawable.ic_benefit_pickup)
            else -> iconView.setImageResource(R.drawable.ic_benefit_nearby)
        }
    }
}
