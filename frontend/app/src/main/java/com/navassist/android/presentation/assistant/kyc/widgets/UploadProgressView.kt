package com.navassist.android.presentation.assistant.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class UploadProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val progressBar: ProgressBar
    private val tvProgressPercent: TextView

    init {
        orientation = HORIZONTAL
        val density = context.resources.displayMetrics.density

        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LayoutParams(0, (6 * density).toInt(), 1f)
            max = 100
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
        }

        tvProgressPercent = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = (10 * density).toInt()
            }
            text = "0%"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        addView(progressBar)
        addView(tvProgressPercent)
    }

    fun setProgress(progressPct: Int) {
        val p = progressPct.coerceIn(0, 100)
        progressBar.progress = p
        tvProgressPercent.text = "$p%"
    }
}
