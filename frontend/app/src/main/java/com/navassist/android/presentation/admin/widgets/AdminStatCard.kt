package com.navassist.android.presentation.admin.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class AdminStatCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvTitle: TextView
    private val tvValue: TextView
    private val tvSubtitle: TextView

    init {
        radius = (18 * context.resources.displayMetrics.density)
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

        tvTitle = TextView(context).apply {
            text = "STAT TITLE"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvValue = TextView(context).apply {
            text = "0"
            textSize = 28f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (4 * density).toInt(), 0, (2 * density).toInt())
        }

        tvSubtitle = TextView(context).apply {
            text = "+0.0% vs yesterday"
            textSize = 11f
            setTextColor(Color.parseColor("#22C55E"))
        }

        rootLayout.addView(tvTitle)
        rootLayout.addView(tvValue)
        rootLayout.addView(tvSubtitle)

        addView(rootLayout)
    }

    fun bindStat(title: String, value: Int, subText: String = "Live count", accentHex: String = "#FAFAFA") {
        tvTitle.text = title.uppercase()
        tvSubtitle.text = subText
        tvValue.setTextColor(Color.parseColor(accentHex))
        animateInt(tvValue, 0, value)
    }

    fun bindAmount(title: String, amount: Double, subText: String = "+18.5% growth", accentHex: String = "#22C55E") {
        tvTitle.text = title.uppercase()
        tvSubtitle.text = subText
        tvValue.setTextColor(Color.parseColor(accentHex))
        animateFloat(tvValue, 0.0f, amount.toFloat(), "₹%.2f")
    }

    private fun animateInt(tv: TextView, start: Int, end: Int) {
        val anim = ValueAnimator.ofInt(start, end).apply {
            duration = 600
            addUpdateListener { animation ->
                tv.text = animation.animatedValue.toString()
            }
        }
        anim.start()
    }

    private fun animateFloat(tv: TextView, start: Float, end: Float, formatPattern: String) {
        val anim = ValueAnimator.ofFloat(start, end).apply {
            duration = 800
            addUpdateListener { animation ->
                val v = animation.animatedValue as Float
                tv.text = String.format(formatPattern, v)
            }
        }
        anim.start()
    }
}
