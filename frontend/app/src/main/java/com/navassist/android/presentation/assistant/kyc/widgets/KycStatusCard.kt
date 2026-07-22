package com.navassist.android.presentation.assistant.kyc.widgets

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class KycStatusCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val ivStatusIcon: ImageView
    private val tvStatusTitle: TextView
    private val tvStatusSubtitle: TextView
    private val tvReviewTime: TextView
    private var pulseAnimator: ObjectAnimator? = null

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#111113"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (20 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        ivStatusIcon = ImageView(context).apply {
            val size = (44 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (14 * density).toInt()
            }
            setImageResource(R.drawable.ic_benefit_safety)
            setColorFilter(Color.parseColor("#71717A"))
        }

        val col = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvStatusTitle = TextView(context).apply {
            text = "Verification Not Submitted"
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvStatusSubtitle = TextView(context).apply {
            text = "Please submit your government identity documents below."
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        col.addView(tvStatusTitle)
        col.addView(tvStatusSubtitle)

        topRow.addView(ivStatusIcon)
        topRow.addView(col)

        tvReviewTime = TextView(context).apply {
            text = "⏱️ Estimated Review Time: Usually within 24 hours"
            textSize = 12f
            setTextColor(Color.parseColor("#71717A"))
            setPadding(0, (14 * density).toInt(), 0, 0)
        }

        rootLayout.addView(topRow)
        rootLayout.addView(tvReviewTime)

        addView(rootLayout)
    }

    fun setStatus(status: String, message: String? = null, rejectionReason: String? = null) {
        pulseAnimator?.cancel()
        pulseAnimator = null

        when (status.uppercase()) {
            "PENDING" -> {
                setCardBackgroundColor(Color.parseColor("#18181B"))
                strokeColor = Color.parseColor("#F59E0B")
                ivStatusIcon.setImageResource(R.drawable.ic_benefit_safety)
                ivStatusIcon.setColorFilter(Color.parseColor("#F59E0B"))
                tvStatusTitle.text = "Documents Under Review"
                tvStatusTitle.setTextColor(Color.parseColor("#F59E0B"))
                tvStatusSubtitle.text = message ?: "Your submitted identity documents are being reviewed by administrators."
                tvReviewTime.visibility = View.VISIBLE

                pulseAnimator = ObjectAnimator.ofFloat(ivStatusIcon, View.ALPHA, 0.4f, 1.0f, 0.4f).apply {
                    duration = 1200
                    repeatCount = ValueAnimator.INFINITE
                    start()
                }
            }
            "VERIFIED", "APPROVED" -> {
                setCardBackgroundColor(Color.parseColor("#111113"))
                strokeColor = Color.parseColor("#22C55E")
                ivStatusIcon.setImageResource(R.drawable.ic_benefit_safety)
                ivStatusIcon.setColorFilter(Color.parseColor("#22C55E"))
                tvStatusTitle.text = "Identity Verified ✓"
                tvStatusTitle.setTextColor(Color.parseColor("#22C55E"))
                tvStatusSubtitle.text = "Congratulations! Your identity verification is complete and active."
                tvReviewTime.visibility = View.GONE
            }
            "REJECTED" -> {
                setCardBackgroundColor(Color.parseColor("#18181B"))
                strokeColor = Color.parseColor("#EF4444")
                ivStatusIcon.setImageResource(R.drawable.ic_feature_safety)
                ivStatusIcon.setColorFilter(Color.parseColor("#EF4444"))
                tvStatusTitle.text = "Verification Action Required"
                tvStatusTitle.setTextColor(Color.parseColor("#EF4444"))
                tvStatusSubtitle.text = rejectionReason ?: "Your document submission was rejected. Please re-upload clear photos."
                tvReviewTime.visibility = View.GONE
            }
            else -> {
                setCardBackgroundColor(Color.parseColor("#111113"))
                strokeColor = Color.parseColor("#27272A")
                ivStatusIcon.setColorFilter(Color.parseColor("#71717A"))
                tvStatusTitle.text = "Verification Not Submitted"
                tvStatusTitle.setTextColor(Color.parseColor("#FAFAFA"))
                tvStatusSubtitle.text = "Please submit your government identity documents below to get approved."
                tvReviewTime.visibility = View.GONE
            }
        }
    }
}
