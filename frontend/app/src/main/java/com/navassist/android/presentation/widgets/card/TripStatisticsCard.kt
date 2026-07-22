package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class TripStatisticsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 24f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val titleView = TextView(context).apply {
            text = "Trip Statistics"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        val metricsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (16 * density).toInt()
            }
        }

        val col1 = createMetricColumn("18.5 km", "Total Distance")
        val col2 = createMetricColumn("32 min", "Travel Time")
        val col3 = createMetricColumn("34 km/h", "Avg. Speed")

        metricsRow.addView(col1)
        metricsRow.addView(col2)
        metricsRow.addView(col3)

        container.addView(titleView)
        container.addView(metricsRow)

        addView(container)
    }

    private fun createMetricColumn(valueText: String, labelText: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)

            val valView = TextView(context).apply {
                text = valueText
                setTextColor(Color.parseColor("#FAFAFA"))
                textSize = 17f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val lblView = TextView(context).apply {
                text = labelText
                setTextColor(Color.parseColor("#A1A1AA"))
                textSize = 11f
            }

            addView(valView)
            addView(lblView)
        }
    }
}
