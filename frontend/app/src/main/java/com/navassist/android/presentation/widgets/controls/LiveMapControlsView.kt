package com.navassist.android.presentation.widgets.controls

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class LiveMapControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var onRecenterClick: (() -> Unit)? = null
    var onZoomInClick: (() -> Unit)? = null
    var onZoomOutClick: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER

        val density = context.resources.displayMetrics.density

        val btnRecenter = createControlButton(R.drawable.ic_benefit_pickup) { onRecenterClick?.invoke() }
        val btnZoomIn = createControlButton(R.drawable.ic_feature_tracking) { onZoomInClick?.invoke() }

        addView(btnRecenter)
        addView(btnZoomIn)
    }

    private fun createControlButton(iconRes: Int, onClick: () -> Unit): MaterialCardView {
        val density = context.resources.displayMetrics.density
        return MaterialCardView(context).apply {
            layoutParams = LayoutParams((44 * density).toInt(), (44 * density).toInt()).apply {
                bottomMargin = (8 * density).toInt()
            }
            setCardBackgroundColor(Color.parseColor("#18181B"))
            radius = 22f * density
            strokeColor = Color.parseColor("#303038")
            strokeWidth = (1 * density).toInt()
            cardElevation = 6f * density
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            val icon = ImageView(context).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams((20 * density).toInt(), (20 * density).toInt(), Gravity.CENTER)
                setImageResource(iconRes)
                setColorFilter(Color.parseColor("#FAFAFA"))
            }
            addView(icon)
        }
    }
}
