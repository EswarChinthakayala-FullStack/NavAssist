package com.navassist.android.presentation.profile.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class ProfileCompletionCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val progressBar: ProgressBar
    val tvPctLabel: TextView
    val tvChecklistItems: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, (8 * density).toInt())
        }

        val tvTitle = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "PROFILE COMPLETION"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvPctLabel = TextView(context).apply {
            text = "85%"
            textSize = 14f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        topRow.addView(tvTitle)
        topRow.addView(tvPctLabel)

        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (6 * density).toInt()).apply {
                bottomMargin = (10 * density).toInt()
            }
            max = 100
            progress = 85
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
        }

        tvChecklistItems = TextView(context).apply {
            text = "✔ Photo  ✔ Bio  ✔ Aadhaar  ✔ Languages  ✔ Skills"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
        }

        rootLayout.addView(topRow)
        rootLayout.addView(progressBar)
        rootLayout.addView(tvChecklistItems)

        addView(rootLayout)
    }

    fun setCompletionPct(pct: Int) {
        val p = pct.coerceIn(0, 100)
        progressBar.progress = p
        tvPctLabel.text = "$p%"
    }
}
