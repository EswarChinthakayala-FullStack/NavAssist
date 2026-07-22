package com.navassist.android.presentation.admin.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class KycStatisticsCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvPendingCount: TextView
    private val tvApprovedToday: TextView
    private val tvRejectedToday: TextView
    private val tvAvgReviewTime: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvTitle = TextView(context).apply {
            text = "VERIFICATION QUEUE SUMMARY"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (10 * density).toInt())
        }

        val row1 = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }
        val col1 = createCol("Pending Review", "0", "#F59E0B").also { tvPendingCount = it.second }
        val col2 = createCol("Approved Today", "0", "#22C55E").also { tvApprovedToday = it.second }
        row1.addView(col1.first); row1.addView(col2.first)

        val row2 = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (12 * density).toInt(), 0, 0)
        }
        val col3 = createCol("Rejected Today", "0", "#EF4444").also { tvRejectedToday = it.second }
        val col4 = createCol("Avg. Review Time", "12m", "#FAFAFA").also { tvAvgReviewTime = it.second }
        row2.addView(col3.first); row2.addView(col4.first)

        rootLayout.addView(tvTitle)
        rootLayout.addView(row1)
        rootLayout.addView(row2)

        addView(rootLayout)
    }

    private fun createCol(label: String, valStr: String, colorHex: String): Pair<LinearLayout, TextView> {
        val density = context.resources.displayMetrics.density
        val col = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvL = TextView(context).apply { text = label; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        val tvV = TextView(context).apply {
            text = valStr
            textSize = 18f
            setTextColor(Color.parseColor(colorHex))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (2 * density).toInt(), 0, 0)
        }
        col.addView(tvL); col.addView(tvV)
        return Pair(col, tvV)
    }

    fun bindStats(pending: Int, approved: Int = 14, rejected: Int = 2, avgTime: String = "12m") {
        tvPendingCount.text = pending.toString()
        tvApprovedToday.text = approved.toString()
        tvRejectedToday.text = rejected.toString()
        tvAvgReviewTime.text = avgTime
    }
}
