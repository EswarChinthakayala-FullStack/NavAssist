package com.navassist.android.presentation.wallet.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.navassist.android.R

class EmptyTransactionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val pad = (32 * density).toInt()
        setPadding(pad, pad, pad, pad)

        val ivWallet = ImageView(context).apply {
            val size = (64 * density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                bottomMargin = (16 * density).toInt()
            }
            setImageResource(R.drawable.ic_feature_payments)
            setColorFilter(Color.parseColor("#3F3F46"))
        }

        val tvHeading = TextView(context).apply {
            text = "No Transactions Yet"
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val tvSub = TextView(context).apply {
            text = "Your wallet top-ups, booking payments, and refunds will appear here."
            textSize = 14f
            setTextColor(Color.parseColor("#71717A"))
            gravity = Gravity.CENTER
            setPadding(0, (6 * density).toInt(), 0, 0)
        }

        addView(ivWallet)
        addView(tvHeading)
        addView(tvSub)
    }
}
