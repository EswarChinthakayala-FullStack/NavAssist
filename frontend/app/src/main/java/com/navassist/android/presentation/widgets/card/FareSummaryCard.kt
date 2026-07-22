package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.presentation.widgets.row.InfoRowView

class FareSummaryCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val rowAssistantFee: InfoRowView
    private val rowPlatformFee: InfoRowView
    private val rowTaxes: InfoRowView
    private val rowTotal: InfoRowView

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

        val headerText = TextView(context).apply {
            text = "Fare Breakdown"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val density = context.resources.displayMetrics.density

        rowAssistantFee = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (12 * density).toInt()
            }
        }

        rowPlatformFee = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
        }

        rowTaxes = InfoRowView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
        }

        val divider = android.view.View(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (1 * density).toInt()).apply {
                topMargin = (14 * density).toInt()
                bottomMargin = (14 * density).toInt()
            }
            setBackgroundColor(Color.parseColor("#27272A"))
        }

        rowTotal = InfoRowView(context)

        container.addView(headerText)
        container.addView(rowAssistantFee)
        container.addView(rowPlatformFee)
        container.addView(rowTaxes)
        container.addView(divider)
        container.addView(rowTotal)

        addView(container)

        setFare(240.0)
    }

    fun setFare(totalAmount: Double) {
        val baseFee = (totalAmount * 0.85).toInt()
        val platformFee = (totalAmount * 0.10).toInt()
        val taxes = (totalAmount * 0.05).toInt()

        rowAssistantFee.setInfo("Assistant Base Fare", "₹$baseFee")
        rowPlatformFee.setInfo("Platform & Safety Fee", "₹$platformFee")
        rowTaxes.setInfo("GST & Service Tax", "₹$taxes")
        rowTotal.setInfo("Total Payable Amount", "₹${totalAmount.toInt()}", isHighlight = true)
    }
}
