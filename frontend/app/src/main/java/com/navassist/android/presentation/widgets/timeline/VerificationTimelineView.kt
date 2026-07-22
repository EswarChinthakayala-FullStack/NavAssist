package com.navassist.android.presentation.widgets.timeline

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.navassist.android.R

class VerificationTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        val steps = listOf(
            "Identity Documents Submitted",
            "Aadhaar & Government ID Verified",
            "Phone & OTP Confirmed",
            "Background Security Check Completed",
            "Approved Travel Assistant Status Active"
        )

        val density = context.resources.displayMetrics.density
        steps.forEachIndexed { index, stepTitle ->
            val row = LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                    topMargin = if (index == 0) 0 else (12 * density).toInt()
                }
            }

            val iconView = ImageView(context).apply {
                layoutParams = LayoutParams((20 * density).toInt(), (20 * density).toInt())
                setImageResource(R.drawable.ic_benefit_safety)
                setColorFilter(Color.parseColor("#22C55E"))
            }

            val textView = TextView(context).apply {
                layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = (12 * density).toInt()
                }
                text = stepTitle
                setTextColor(Color.parseColor("#FAFAFA"))
                textSize = 13f
            }

            row.addView(iconView)
            row.addView(textView)
            addView(row)
        }
    }
}
