package com.navassist.android.presentation.widgets.rating

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView

class RatingStarsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val starsTextView: TextView
    private val ratingValueTextView: TextView

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val density = context.resources.displayMetrics.density

        starsTextView = TextView(context).apply {
            text = "★★★★★"
            setTextColor(Color.parseColor("#F59E0B"))
            textSize = 14f
        }

        ratingValueTextView = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                marginStart = (6 * density).toInt()
            }
            text = "4.9"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        addView(starsTextView)
        addView(ratingValueTextView)
    }

    fun setRating(rating: Float) {
        val fullStars = rating.toInt()
        val stars = "★".repeat(fullStars.coerceIn(1, 5))
        starsTextView.text = stars
        ratingValueTextView.text = String.format("%.1f", rating)
    }
}
