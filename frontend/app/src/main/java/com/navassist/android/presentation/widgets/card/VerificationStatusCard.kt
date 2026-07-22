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

class VerificationStatusCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val descView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#27272A"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#3F3F46")
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
            setImageResource(R.drawable.ic_benefit_safety)
            setColorFilter(Color.parseColor("#22C55E"))
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * density).toInt()
            }
        }

        titleView = TextView(context).apply {
            text = "Government ID Verified"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        descView = TextView(context).apply {
            text = "Official identity document validated"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        textLayout.addView(titleView)
        textLayout.addView(descView)

        container.addView(iconView)
        container.addView(textLayout)
        addView(container)
    }

    fun setVerification(title: String, desc: String) {
        titleView.text = title
        descView.text = desc
    }
}
