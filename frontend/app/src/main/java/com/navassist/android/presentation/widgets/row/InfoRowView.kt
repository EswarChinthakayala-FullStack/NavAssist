package com.navassist.android.presentation.widgets.row

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class InfoRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val labelView: TextView
    private val valueView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        labelView = TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 13f
        }

        valueView = TextView(context).apply {
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(labelView)
        addView(valueView)
    }

    fun setInfo(label: String, value: String, isHighlight: Boolean = false) {
        labelView.text = label
        valueView.text = value
        if (isHighlight) {
            valueView.setTextColor(Color.parseColor("#22C55E"))
            valueView.textSize = 16f
        } else {
            valueView.setTextColor(Color.parseColor("#FAFAFA"))
            valueView.textSize = 13f
        }
    }
}
