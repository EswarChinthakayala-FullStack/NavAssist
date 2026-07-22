package com.navassist.android.presentation.widgets.card

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import coil3.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.navassist.android.R
import com.navassist.android.domain.model.Assistant
import com.navassist.android.presentation.widgets.badge.VerifiedBadgeView

class AssistantPreviewCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val ivAvatar: ShapeableImageView
    private val tvName: TextView
    private val tvRating: TextView

    init {
        setCardBackgroundColor(Color.parseColor("#18181B"))
        radius = 26f * context.resources.displayMetrics.density
        strokeColor = Color.parseColor("#303038")
        strokeWidth = (1 * context.resources.displayMetrics.density).toInt()
        cardElevation = 6f * context.resources.displayMetrics.density

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
            setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
        }

        val density = context.resources.displayMetrics.density

        ivAvatar = ShapeableImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams((60 * density).toInt(), (60 * density).toInt())
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            setImageResource(R.drawable.ic_app_logo)
            shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(30 * density)
                .build()
        }

        val textLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = (14 * density).toInt()
            }
        }

        val nameRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        tvName = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Assistant Selected"
            setTextColor(Color.parseColor("#FAFAFA"))
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val badgeVerified = VerifiedBadgeView(context)

        nameRow.addView(tvName)
        nameRow.addView(badgeVerified)

        tvRating = TextView(context).apply {
            text = "★ 4.9 · Verified Partner"
            setTextColor(Color.parseColor("#F59E0B"))
            textSize = 12f
        }

        textLayout.addView(nameRow)
        textLayout.addView(tvRating)

        container.addView(ivAvatar)
        container.addView(textLayout)
        addView(container)
    }

    fun setAssistant(assistant: Assistant?) {
        if (assistant != null) {
            tvName.text = assistant.name
            tvRating.text = "★ ${assistant.rating} · ${assistant.totalTrips} Trips Completed"
            if (!assistant.photoUrl.isNullOrEmpty()) {
                ivAvatar.load(assistant.photoUrl)
            }
        } else {
            tvName.text = "First Available Verified Assistant"
            tvRating.text = "★ 4.9 · Instant Auto Match"
        }
    }
}
