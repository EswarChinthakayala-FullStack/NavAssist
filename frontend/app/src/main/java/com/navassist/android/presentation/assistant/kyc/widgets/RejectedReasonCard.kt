package com.navassist.android.presentation.assistant.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class RejectedReasonCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val tvReason: TextView
    val btnReupload: MaterialButton

    var onReuploadClickListener: (() -> Unit)? = null

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#EF4444")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvTitle = TextView(context).apply {
            text = "REJECTION REASON & AUDIT NOTES"
            textSize = 11f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
        }

        tvReason = TextView(context).apply {
            text = "The uploaded document photo was blurry or incomplete. Please ensure full card boundaries and text are legible."
            textSize = 13f
            setTextColor(Color.parseColor("#FAFAFA"))
            setPadding(0, (6 * density).toInt(), 0, (14 * density).toInt())
        }

        btnReupload = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (46 * density).toInt())
            text = "Re-upload Documents"
            textSize = 14f
            setTextColor(Color.parseColor("#FFFFFF"))
            setBackgroundColor(Color.parseColor("#EF4444"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnReupload.setOnClickListener { onReuploadClickListener?.invoke() }

        rootLayout.addView(tvTitle)
        rootLayout.addView(tvReason)
        rootLayout.addView(btnReupload)

        addView(rootLayout)
    }

    fun setReason(reasonStr: String) {
        tvReason.text = reasonStr
    }
}
