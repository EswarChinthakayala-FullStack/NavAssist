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

class ScheduleOptionCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val iconView: ImageView
    private val titleView: TextView
    private val subtitleView: TextView

    init {
        radius = 24f * context.resources.displayMetrics.density
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density
        iconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((28 * density).toInt(), (28 * density).toInt())
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (14 * density).toInt()
            }
        }

        titleView = TextView(context).apply {
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        subtitleView = TextView(context).apply {
            textSize = 12f
        }

        textLayout.addView(titleView)
        textLayout.addView(subtitleView)

        container.addView(iconView)
        container.addView(textLayout)
        addView(container)

        setSelectedState(false)
        isClickable = true
        isFocusable = true
    }

    fun setOption(title: String, subtitle: String, iconResId: Int) {
        titleView.text = title
        subtitleView.text = subtitle
        iconView.setImageResource(iconResId)
    }

    fun setSelectedState(isSelected: Boolean) {
        if (isSelected) {
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            strokeColor = Color.parseColor("#FFFFFF")
            titleView.setTextColor(Color.parseColor("#09090B"))
            subtitleView.setTextColor(Color.parseColor("#52525B"))
            iconView.setColorFilter(Color.parseColor("#09090B"))
        } else {
            setCardBackgroundColor(Color.parseColor("#18181B"))
            strokeColor = Color.parseColor("#303038")
            titleView.setTextColor(Color.parseColor("#FAFAFA"))
            subtitleView.setTextColor(Color.parseColor("#A1A1AA"))
            iconView.setColorFilter(Color.parseColor("#FAFAFA"))
        }
    }
}
