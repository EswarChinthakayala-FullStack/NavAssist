package com.navassist.android.presentation.widgets.search

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import com.navassist.android.R

class SearchBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val searchEditText: EditText
    val targetImageView: ImageView

    var onSearchClickListener: (() -> Unit)? = null
    var onTargetClickListener: (() -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val density = context.resources.displayMetrics.density
        val paddingHorizontalPx = (16 * density).toInt()
        val paddingVerticalPx = (12 * density).toInt()

        setPadding(paddingHorizontalPx, paddingVerticalPx, paddingHorizontalPx, paddingVerticalPx)
        setBackgroundResource(R.drawable.bg_input_field)

        // Search Icon
        val searchIcon = ImageView(context).apply {
            layoutParams = LayoutParams((20 * density).toInt(), (20 * density).toInt())
            setImageResource(R.drawable.ic_feature_tracking)
            contentDescription = "Search"
        }

        // Search EditText
        searchEditText = EditText(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (12 * density).toInt()
                marginEnd = (12 * density).toInt()
            }
            background = null
            hint = "Where would you like to go?"
            setHintTextColor(Color.parseColor("#71717A"))
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 15f
            isFocusable = false
            isClickable = true
        }

        // Target GPS Icon
        targetImageView = ImageView(context).apply {
            layoutParams = LayoutParams((24 * density).toInt(), (24 * density).toInt())
            setImageResource(R.drawable.ic_benefit_nearby)
            contentDescription = "Current Location"
            isClickable = true
            isFocusable = true
        }

        addView(searchIcon)
        addView(searchEditText)
        addView(targetImageView)

        searchEditText.setOnClickListener {
            onSearchClickListener?.invoke()
        }

        setOnClickListener {
            onSearchClickListener?.invoke()
        }

        targetImageView.setOnClickListener {
            onTargetClickListener?.invoke()
        }
    }
}
