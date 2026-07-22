package com.navassist.android.presentation.assistant.booking.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.navassist.android.R

class BookingExpiredView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val btnDismiss: MaterialButton
    private val tvHeading: TextView
    private val tvSubtitle: TextView

    var onDismissClickListener: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val pad = (24 * density).toInt()
        setPadding(pad, pad, pad, pad)

        val ivWarning = ImageView(context).apply {
            val size = (64 * density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                bottomMargin = (16 * density).toInt()
            }
            setImageResource(R.drawable.ic_feature_safety)
            setColorFilter(Color.parseColor("#EF4444"))
        }

        tvHeading = TextView(context).apply {
            text = "Booking Request Expired"
            textSize = 20f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        tvSubtitle = TextView(context).apply {
            text = "This request was not accepted in time or has been re-assigned to another available guide."
            textSize = 14f
            setTextColor(Color.parseColor("#A1A1AA"))
            gravity = Gravity.CENTER
            setPadding(0, (8 * density).toInt(), 0, (20 * density).toInt())
        }

        btnDismiss = MaterialButton(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (50 * density).toInt())
            text = "Return to Dashboard"
            textSize = 15f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnDismiss.setOnClickListener { onDismissClickListener?.invoke() }

        addView(ivWarning)
        addView(tvHeading)
        addView(tvSubtitle)
        addView(btnDismiss)
    }
}
