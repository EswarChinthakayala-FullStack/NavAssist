package com.navassist.android.presentation.profile.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class LanguagesCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val chipGroup: ChipGroup

    init {
        radius = (20 * context.resources.displayMetrics.density)
        setCardBackgroundColor(Color.parseColor("#18181B"))
        strokeColor = Color.parseColor("#27272A")
        strokeWidth = (1.5f * context.resources.displayMetrics.density).toInt()
        cardElevation = 0f

        val density = context.resources.displayMetrics.density
        val rootLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvTitle = TextView(context).apply {
            text = "LANGUAGES SPOKEN"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (10 * density).toInt())
        }

        chipGroup = ChipGroup(context).apply {
            isSingleSelection = false
        }

        val languages = listOf("English", "Telugu", "Hindi", "Tamil", "Kannada")
        languages.forEachIndexed { idx, lang ->
            val chip = Chip(context).apply {
                text = lang
                isCheckable = true
                isChecked = idx == 0 || idx == 1 || idx == 2
                setChipBackgroundColorResource(android.R.color.transparent)
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#111113"))
                chipStrokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
                chipStrokeWidth = 1.5f * density
                setTextColor(Color.parseColor("#FAFAFA"))
            }
            chipGroup.addView(chip)
        }

        rootLayout.addView(tvTitle)
        rootLayout.addView(chipGroup)

        addView(rootLayout)
    }
}
