package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class WalletBalanceCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val balanceTextView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 28f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density
        val iconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((28 * density).toInt(), (28 * density).toInt())
            setImageResource(R.drawable.ic_feature_tracking)
            setColorFilter(Color.parseColor("#22C55E"))
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (14 * density).toInt()
            }
        }

        val titleView = TextView(context).apply {
            text = "NavAssist Wallet Balance"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        balanceTextView = TextView(context).apply {
            text = "₹1,245 Available"
            setTextColor(Color.parseColor("#22C55E"))
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        textLayout.addView(titleView)
        textLayout.addView(balanceTextView)

        container.addView(iconView)
        container.addView(textLayout)
        addView(container)
    }

    fun setBalance(balance: Double) {
        balanceTextView.text = "₹${balance.toInt()} Available"
    }
}
