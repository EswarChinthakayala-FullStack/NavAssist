package com.navassist.android.presentation.widgets.controls

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class MapFloatingControls @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    var onRecenterClickListener: (() -> Unit)? = null

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 20f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 8f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val paddingPx = (10 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density
        val recenterBtn = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(R.drawable.ic_benefit_nearby)
            contentDescription = "Recenter Map"
            isClickable = true
            isFocusable = true
        }

        container.addView(recenterBtn)
        addView(container)

        recenterBtn.setOnClickListener {
            onRecenterClickListener?.invoke()
        }
    }
}
