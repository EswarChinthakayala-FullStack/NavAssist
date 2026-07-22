package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.domain.model.Review
import com.navassist.android.presentation.widgets.rating.RatingStarsView

class ReviewCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val nameView: TextView
    private val ratingStarsView: RatingStarsView
    private val dateView: TextView
    private val commentView: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 20f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        nameView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "John Doe"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        ratingStarsView = RatingStarsView(context)

        topRow.addView(nameView)
        topRow.addView(ratingStarsView)

        val density = context.resources.displayMetrics.density
        dateView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (4 * density).toInt()
            }
            text = "Oct 20, 2026"
            setTextColor(Color.parseColor("#71717A"))
            textSize = 11f
        }

        commentView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = (8 * density).toInt()
            }
            text = "Great service and extremely punctual!"
            setTextColor(Color.parseColor("#D4D4D8"))
            textSize = 13f
        }

        container.addView(topRow)
        container.addView(dateView)
        container.addView(commentView)
        addView(container)
    }

    fun setReview(review: Review) {
        nameView.text = review.reviewerName
        ratingStarsView.setRating(review.rating)
        dateView.text = review.date
        commentView.text = review.comment
    }
}
