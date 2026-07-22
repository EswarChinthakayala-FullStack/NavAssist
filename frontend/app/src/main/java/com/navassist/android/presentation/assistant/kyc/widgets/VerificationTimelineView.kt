package com.navassist.android.presentation.assistant.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView

class VerificationTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val step1Dot: View
    private val step2Dot: View
    private val step3Dot: View
    private val step4Dot: View

    private val step1Line: View
    private val step2Line: View
    private val step3Line: View
    private val step4Line: View

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
            text = "VERIFICATION TIMELINE"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (14 * density).toInt())
        }

        val timelineLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val res1 = createTimelineStep("1. Documents Uploaded", "Front & Back photos attached")
        step1Dot = res1.first; step1Line = res1.second

        val res2 = createTimelineStep("2. Under Review", "Submitted to verification queue")
        step2Dot = res2.first; step2Line = res2.second

        val res3 = createTimelineStep("3. Background Check", "Aadhaar & identity validation")
        step3Dot = res3.first; step3Line = res3.second

        val res4 = createTimelineStep("4. Verified & Active", "Ready to accept passenger rides", isLast = true)
        step4Dot = res4.first; step4Line = res4.second

        timelineLayout.addView(res1.third)
        timelineLayout.addView(res2.third)
        timelineLayout.addView(res3.third)
        timelineLayout.addView(res4.third)

        rootLayout.addView(tvTitle)
        rootLayout.addView(timelineLayout)

        addView(rootLayout)
    }

    private fun createTimelineStep(title: String, desc: String, isLast: Boolean = false): Triple<View, View, LinearLayout> {
        val density = context.resources.displayMetrics.density
        val stepRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val colIndicator = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams((24 * density).toInt(), LinearLayout.LayoutParams.MATCH_PARENT).apply {
                marginEnd = (12 * density).toInt()
            }
        }

        val dot = View(context).apply {
            val size = (12 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                topMargin = (4 * density).toInt()
            }
            val drawable = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.parseColor("#27272A"))
            }
            background = drawable
        }

        val line = View(context).apply {
            layoutParams = LinearLayout.LayoutParams((2 * density).toInt(), (28 * density).toInt())
            setBackgroundColor(Color.parseColor("#27272A"))
            visibility = if (isLast) View.GONE else View.VISIBLE
        }

        colIndicator.addView(dot)
        colIndicator.addView(line)

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, (14 * density).toInt())
        }

        val tvT = TextView(context).apply {
            text = title
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvD = TextView(context).apply {
            text = desc
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvT)
        colText.addView(tvD)

        stepRow.addView(colIndicator)
        stepRow.addView(colText)

        return Triple(dot, line, stepRow)
    }

    fun updateProgress(status: String) {
        val emerald = Color.parseColor("#22C55E")
        val gray = Color.parseColor("#27272A")
        val amber = Color.parseColor("#F59E0B")

        when (status.uppercase()) {
            "PENDING" -> {
                setDotColor(step1Dot, emerald); setLineColor(step1Line, emerald)
                setDotColor(step2Dot, amber); setLineColor(step2Line, gray)
                setDotColor(step3Dot, gray); setLineColor(step3Line, gray)
                setDotColor(step4Dot, gray)
            }
            "VERIFIED", "APPROVED" -> {
                setDotColor(step1Dot, emerald); setLineColor(step1Line, emerald)
                setDotColor(step2Dot, emerald); setLineColor(step2Line, emerald)
                setDotColor(step3Dot, emerald); setLineColor(step3Line, emerald)
                setDotColor(step4Dot, emerald)
            }
            else -> {
                setDotColor(step1Dot, gray); setLineColor(step1Line, gray)
                setDotColor(step2Dot, gray); setLineColor(step2Line, gray)
                setDotColor(step3Dot, gray); setLineColor(step3Line, gray)
                setDotColor(step4Dot, gray)
            }
        }
    }

    private fun setDotColor(v: View, color: Int) {
        val drawable = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.OVAL
            setColor(color)
        }
        v.background = drawable
    }

    private fun setLineColor(v: View, color: Int) {
        v.setBackgroundColor(color)
    }
}
