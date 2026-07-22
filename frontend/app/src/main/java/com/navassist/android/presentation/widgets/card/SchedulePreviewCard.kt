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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchedulePreviewCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val titleView: TextView
    private val descView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 4f * context.resources.displayMetrics.density

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
            setColorFilter(Color.parseColor("#FAFAFA"))
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (14 * density).toInt()
            }
        }

        titleView = TextView(context).apply {
            text = "Travel Mode"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        descView = TextView(context).apply {
            text = "Ride Now (Immediate Matching)"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        textLayout.addView(titleView)
        textLayout.addView(descView)

        container.addView(iconView)
        container.addView(textLayout)
        addView(container)
    }

    fun setScheduledTimestamp(timestamp: Long?) {
        if (timestamp != null) {
            val sdf = SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault())
            titleView.text = "Scheduled Travel Time"
            descView.text = sdf.format(Date(timestamp))
            descView.setTextColor(Color.parseColor("#FAFAFA"))
        } else {
            titleView.text = "Travel Mode"
            descView.text = "Ride Now (Immediate Matching)"
            descView.setTextColor(Color.parseColor("#22C55E"))
        }
    }
}
