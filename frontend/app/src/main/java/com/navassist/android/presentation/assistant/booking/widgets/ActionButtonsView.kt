package com.navassist.android.presentation.assistant.booking.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.google.android.material.button.MaterialButton

class ActionButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val btnDecline: MaterialButton
    val btnAccept: MaterialButton
    val pbLoading: ProgressBar

    var onAcceptClickListener: (() -> Unit)? = null
    var onDeclineClickListener: (() -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density

        btnDecline = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LayoutParams(0, (54 * density).toInt(), 1f).apply {
                marginEnd = (10 * density).toInt()
            }
            text = "Decline Request"
            textSize = 15f
            setTextColor(Color.parseColor("#EF4444"))
            setBackgroundColor(Color.parseColor("#26EF4444"))
            cornerRadius = (16 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnAccept = MaterialButton(context).apply {
            layoutParams = LayoutParams(0, (54 * density).toInt(), 2f).apply {
                marginStart = (6 * density).toInt()
            }
            text = "Accept Ride Request"
            textSize = 16f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (16 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        pbLoading = ProgressBar(context).apply {
            layoutParams = LayoutParams((32 * density).toInt(), (32 * density).toInt()).apply {
                gravity = Gravity.CENTER
            }
            visibility = GONE
        }

        btnDecline.setOnClickListener {
            disableButtons()
            onDeclineClickListener?.invoke()
        }

        btnAccept.setOnClickListener {
            showLoading()
            onAcceptClickListener?.invoke()
        }

        addView(btnDecline)
        addView(btnAccept)
        addView(pbLoading)
    }

    fun showLoading() {
        btnAccept.visibility = GONE
        btnDecline.visibility = GONE
        pbLoading.visibility = VISIBLE
    }

    fun hideLoading() {
        btnAccept.visibility = VISIBLE
        btnDecline.visibility = VISIBLE
        btnAccept.isEnabled = true
        btnDecline.isEnabled = true
        pbLoading.visibility = GONE
    }

    fun disableButtons() {
        btnAccept.isEnabled = false
        btnDecline.isEnabled = false
    }
}
