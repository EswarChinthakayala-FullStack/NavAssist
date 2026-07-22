package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.journey.adapter.JourneyTimelineAdapter
import com.navassist.android.presentation.journey.adapter.TimelineItem

class LiveTrackingBottomSheet @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val timelineAdapter = JourneyTimelineAdapter()

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 32f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 12f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val handle = android.view.View(context).apply {
            val density = context.resources.displayMetrics.density
            layoutParams = LinearLayout.LayoutParams((36 * density).toInt(), (4 * density).toInt()).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                bottomMargin = (12 * density).toInt()
            }
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.parseColor("#3F3F46"))
                cornerRadius = 2f * density
            }
        }

        val titleView = TextView(context).apply {
            text = "Live Tracking & History"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density
        val recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
            layoutManager = LinearLayoutManager(context)
            adapter = timelineAdapter
            isNestedScrollingEnabled = false
        }

        container.addView(handle)
        container.addView(titleView)
        container.addView(recyclerView)

        addView(container)

        val mockTimeline = listOf(
            TimelineItem("1", "Booking Confirmed", "Booking #BK_10293 verified with payment", "03:28 PM", true),
            TimelineItem("2", "Travel Assistant Assigned", "Vikram Sharma assigned as primary assistant", "03:29 PM", true),
            TimelineItem("3", "Assistant En Route", "Assistant is driving toward pickup location", "03:32 PM", true)
        )
        timelineAdapter.submitList(mockTimeline)
    }
}
