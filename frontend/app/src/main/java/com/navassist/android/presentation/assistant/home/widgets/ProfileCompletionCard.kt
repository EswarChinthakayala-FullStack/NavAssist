package com.navassist.android.presentation.assistant.home.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class ProfileCompletionCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvTitle: TextView
    private val tvSubtitle: TextView
    private val pbCompletion: ProgressBar
    private val tvPercentage: TextView

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val ivShield = ImageView(context).apply {
            val size = (40 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (14 * density).toInt()
            }
            setImageResource(R.drawable.ic_benefit_safety)
            setColorFilter(Color.parseColor("#FAFAFA"))
        }

        val infoCol = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvTitle = TextView(context).apply {
            text = "Profile Verification Complete"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvSubtitle = TextView(context).apply {
            text = "Aadhaar & Background Check Verified ✓"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            setPadding(0, (2 * density).toInt(), 0, (8 * density).toInt())
        }

        pbCompletion = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (6 * density).toInt())
            max = 100
            progress = 100
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            progressBackgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
        }

        infoCol.addView(tvTitle)
        infoCol.addView(tvSubtitle)
        infoCol.addView(pbCompletion)

        tvPercentage = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = (12 * density).toInt()
            }
            text = "100%"
            textSize = 16f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        rootLayout.addView(ivShield)
        rootLayout.addView(infoCol)
        rootLayout.addView(tvPercentage)

        addView(rootLayout)
    }

    fun setCompletion(pct: Int, status: String) {
        val verified = status.equals("VERIFIED", ignoreCase = true)
        if (verified) {
            tvTitle.text = "Profile Verified & Approved"
            tvSubtitle.text = "All documents & background checks clear ✓"
            tvSubtitle.setTextColor(Color.parseColor("#22C55E"))
            pbCompletion.progress = 100
            tvPercentage.text = "100%"
            tvPercentage.setTextColor(Color.parseColor("#22C55E"))
        } else {
            tvTitle.text = "Verification Pending"
            tvSubtitle.text = "Document review in progress..."
            tvSubtitle.setTextColor(Color.parseColor("#F59E0B"))
            pbCompletion.progress = pct.coerceIn(0, 99)
            tvPercentage.text = "$pct%"
            tvPercentage.setTextColor(Color.parseColor("#F59E0B"))
        }
    }
}
