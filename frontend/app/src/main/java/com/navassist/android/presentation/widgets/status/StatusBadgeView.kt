package com.navassist.android.presentation.widgets.status

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.navassist.android.R

open class StatusBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
        setPadding(dpToPx(12), dpToPx(6), dpToPx(12), dpToPx(6))
        textSize = 12f
        setTypeface(typeface, Typeface.BOLD)
        setStatus("PENDING")
    }

    fun setStatus(statusStr: String) {
        val uppercaseStatus = statusStr.uppercase()
        text = uppercaseStatus
        when (uppercaseStatus) {
            "ACCEPTED" -> {
                setBackgroundColor(ContextCompat.getColor(context, R.color.status_accepted_bg))
                setTextColor(ContextCompat.getColor(context, R.color.status_accepted_text))
            }
            "ONGOING" -> {
                setBackgroundColor(ContextCompat.getColor(context, R.color.status_ongoing_bg))
                setTextColor(ContextCompat.getColor(context, R.color.status_ongoing_text))
            }
            "COMPLETED" -> {
                setBackgroundColor(ContextCompat.getColor(context, R.color.status_completed_bg))
                setTextColor(ContextCompat.getColor(context, R.color.status_completed_text))
            }
            "CANCELLED" -> {
                setBackgroundColor(ContextCompat.getColor(context, R.color.status_cancelled_bg))
                setTextColor(ContextCompat.getColor(context, R.color.status_cancelled_text))
            }
            else -> {
                setBackgroundColor(ContextCompat.getColor(context, R.color.status_pending_bg))
                setTextColor(ContextCompat.getColor(context, R.color.status_pending_text))
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
