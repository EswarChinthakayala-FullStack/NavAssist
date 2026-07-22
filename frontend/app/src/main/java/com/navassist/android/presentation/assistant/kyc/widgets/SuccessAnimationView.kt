package com.navassist.android.presentation.assistant.kyc.widgets

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.navassist.android.R

class SuccessAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val btnContinue: MaterialButton

    var onContinueClickListener: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val pad = (24 * density).toInt()
        setPadding(pad, pad, pad, pad)

        val ivShield = ImageView(context).apply {
            val size = (72 * density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                bottomMargin = (16 * density).toInt()
            }
            setImageResource(R.drawable.ic_benefit_safety)
            setColorFilter(Color.parseColor("#22C55E"))
        }

        val tvTitle = TextView(context).apply {
            text = "Verification Complete!"
            textSize = 22f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val tvSub = TextView(context).apply {
            text = "Congratulations! Your identity has been verified. You are now fully approved to start accepting passenger booking requests."
            textSize = 14f
            setTextColor(Color.parseColor("#A1A1AA"))
            gravity = Gravity.CENTER
            setPadding(0, (8 * density).toInt(), 0, (24 * density).toInt())
        }

        btnContinue = MaterialButton(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (50 * density).toInt())
            text = "Go To Assistant Dashboard"
            textSize = 15f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnContinue.setOnClickListener { onContinueClickListener?.invoke() }

        addView(ivShield)
        addView(tvTitle)
        addView(tvSub)
        addView(btnContinue)

        // Scale entrance animation
        ObjectAnimator.ofFloat(ivShield, "scaleX", 0.6f, 1.1f, 1.0f).setDuration(600).start()
        ObjectAnimator.ofFloat(ivShield, "scaleY", 0.6f, 1.1f, 1.0f).setDuration(600).start()
    }
}
