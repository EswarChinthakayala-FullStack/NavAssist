package com.navassist.android.presentation.widgets.stepper

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class BookingStepItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val numberView: TextView
    private val titleView: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density

        numberView = TextView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())
            gravity = Gravity.CENTER
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#09090B"))
        }

        titleView = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = (4 * density).toInt()
            }
            textSize = 10f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#71717A"))
        }

        addView(numberView)
        addView(titleView)
    }

    fun setStepInfo(number: Int, title: String, isCurrent: Boolean, isCompleted: Boolean) {
        titleView.text = title
        numberView.text = number.toString()

        val density = context.resources.displayMetrics.density
        val circleDrawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            if (isCurrent || isCompleted) {
                setColor(Color.parseColor("#FFFFFF"))
            } else {
                setColor(Color.parseColor("#27272A"))
                setStroke((1 * density).toInt(), Color.parseColor("#3F3F46"))
            }
        }

        numberView.background = circleDrawable
        if (isCurrent || isCompleted) {
            numberView.setTextColor(Color.parseColor("#09090B"))
            titleView.setTextColor(Color.parseColor("#FAFAFA"))
        } else {
            numberView.setTextColor(Color.parseColor("#A1A1AA"))
            titleView.setTextColor(Color.parseColor("#71717A"))
        }
    }
}
