package com.navassist.android.presentation.admin.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class TrustScoreCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvScore: TextView
    private val tvPhoneStatus: TextView
    private val tvFraudStatus: TextView

    init {
        radius = (18 * context.resources.displayMetrics.density)
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
            text = "SYSTEM TRUST & SAFETY METRICS"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (10 * density).toInt())
        }

        val row = LinearLayout(context).apply { orientation = LinearLayout.HORIZONTAL }

        val col1 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvL1 = TextView(context).apply { text = "System Trust Score"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvScore = TextView(context).apply { text = "98%"; textSize = 22f; setTextColor(Color.parseColor("#22C55E")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col1.addView(tvL1); col1.addView(tvScore)

        val col2 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvL2 = TextView(context).apply { text = "Phone Verification"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvPhoneStatus = TextView(context).apply { text = "VERIFIED ✓"; textSize = 14f; setTextColor(Color.parseColor("#22C55E")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col2.addView(tvL2); col2.addView(tvPhoneStatus)

        val col3 = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val tvL3 = TextView(context).apply { text = "Fraud Checks"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")) }
        tvFraudStatus = TextView(context).apply { text = "PASSED ✓"; textSize = 14f; setTextColor(Color.parseColor("#22C55E")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        col3.addView(tvL3); col3.addView(tvFraudStatus)

        row.addView(col1)
        row.addView(col2)
        row.addView(col3)

        rootLayout.addView(tvTitle)
        rootLayout.addView(row)

        addView(rootLayout)
    }

    fun bindMetrics(score: Int = 98) {
        tvScore.text = "$score%"
    }
}
