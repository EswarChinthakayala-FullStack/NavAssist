package com.navassist.android.presentation.widgets.card

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.row.InfoRowView

class DiscountPreviewCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val rowOriginal: InfoRowView
    private val rowDiscount: InfoRowView
    private val rowNewTotal: InfoRowView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 26f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (20 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val titleView = TextView(context).apply {
            text = "Updated Fare Preview"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density

        rowOriginal = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
        }

        rowDiscount = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
        }

        val divider = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (1 * density).toInt()).apply {
                topMargin = (12 * density).toInt()
                bottomMargin = (12 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        rowNewTotal = InfoRowView(context)

        container.addView(titleView)
        container.addView(rowOriginal)
        container.addView(rowDiscount)
        container.addView(divider)
        container.addView(rowNewTotal)

        addView(container)
        visibility = GONE
    }

    fun setDiscountPreview(originalAmount: Double, discountAmount: Double) {
        val newTotal = (originalAmount - discountAmount).coerceAtLeast(0.0)
        rowOriginal.setInfo("Original Fare", "₹${originalAmount.toInt()}")
        rowDiscount.setInfo("Promo Discount", "-₹${discountAmount.toInt()}", isHighlight = true)
        rowNewTotal.setInfo("Updated Total Payable", "₹${newTotal.toInt()}", isHighlight = true)
        visibility = VISIBLE
    }
}
