package com.navassist.android.presentation.admin.kyc.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import coil3.load
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R

class DocumentPreviewCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val tvDocTitle: TextView
    val ivPreview: ImageView

    var onPreviewClickListener: (() -> Unit)? = null

    init {
        radius = (18 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (14 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        tvDocTitle = TextView(context).apply {
            text = "DOCUMENT PREVIEW"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, (10 * density).toInt())
        }

        ivPreview = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (160 * density).toInt()
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.parseColor("#111113"))
            setImageResource(R.drawable.ic_feature_safety)
        }

        ivPreview.setOnClickListener { onPreviewClickListener?.invoke() }

        rootLayout.addView(tvDocTitle)
        rootLayout.addView(ivPreview)

        addView(rootLayout)
    }

    fun bindDocument(title: String, imageUrl: String?) {
        tvDocTitle.text = title.uppercase()
        if (!imageUrl.isNull_or_empty()) {
            ivPreview.load(imageUrl)
        } else {
            ivPreview.setImageResource(R.drawable.ic_feature_safety)
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
