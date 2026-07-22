package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ReceiptDownloadCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 22f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val titleView = TextView(context).apply {
            text = "Download Official Receipt"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val subtitleView = TextView(context).apply {
            text = "Save your payment PDF for tax records"
            setTextColor(Color.parseColor("#A1A1AA"))
            textSize = 12f
        }

        textLayout.addView(titleView)
        textLayout.addView(subtitleView)

        val density = context.resources.displayMetrics.density
        val downloadButton = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, (40 * density).toInt()).apply {
                marginStart = (12 * density).toInt()
            }
            text = "Download PDF"
            setTextColor(Color.parseColor("#09090B"))
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            cornerRadius = (14 * density).toInt()
        }

        container.addView(textLayout)
        container.addView(downloadButton)
        addView(container)
    }
}
