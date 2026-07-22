package com.navassist.android.presentation.profile.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider

class AssistantProfessionalCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val etBio: EditText
    val etExperienceYears: EditText
    val sliderRadius: Slider
    val tvRadiusValue: TextView

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
            text = "PROFESSIONAL ASSISTANT DETAILS"
            textSize = 11f
            setTextColor(Color.parseColor("#71717A"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            letterSpacing = 0.05f
            setPadding(0, 0, 0, (12 * density).toInt())
        }

        // Bio Label & Input
        val tvBioLbl = TextView(context).apply { text = "Professional Bio (Max 500 chars)"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        etBio = EditText(context).apply {
            hint = "Introduce your experience, qualifications, and guest support specialization..."
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            setHintTextColor(Color.parseColor("#71717A"))
            setBackgroundColor(Color.parseColor("#111113"))
            minLines = 3
            maxLines = 5
            gravity = android.view.Gravity.TOP
            val p = (10 * density).toInt()
            setPadding(p, p, p, p)
        }

        // Experience Years Input
        val tvExpLbl = TextView(context).apply { text = "Years of Experience"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")); typeface = android.graphics.Typeface.DEFAULT_BOLD; setPadding(0, (12 * density).toInt(), 0, 0) }
        etExperienceYears = EditText(context).apply {
            hint = "e.g. 5"
            textSize = 14f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setTextColor(Color.parseColor("#FAFAFA"))
            setHintTextColor(Color.parseColor("#71717A"))
            setBackgroundColor(Color.parseColor("#111113"))
            val p = (10 * density).toInt()
            setPadding(p, p, p, p)
        }

        // Service Radius Slider
        val radiusRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (12 * density).toInt(), 0, 0)
        }
        val tvRadLbl = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Service Operating Radius"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tvRadiusValue = TextView(context).apply {
            text = "10.0 KM"
            textSize = 12f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        radiusRow.addView(tvRadLbl); radiusRow.addView(tvRadiusValue)

        sliderRadius = Slider(context).apply {
            valueFrom = 5f
            valueTo = 100f
            stepSize = 5f
            value = 10f
            thumbTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FAFAFA"))
            trackActiveTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#22C55E"))
            trackInactiveTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
            addOnChangeListener { _, value, _ ->
                tvRadiusValue.text = "%.1f KM".format(value)
            }
        }

        rootLayout.addView(tvTitle)
        rootLayout.addView(tvBioLbl)
        rootLayout.addView(etBio)
        rootLayout.addView(tvExpLbl)
        rootLayout.addView(etExperienceYears)
        rootLayout.addView(radiusRow)
        rootLayout.addView(sliderRadius)

        addView(rootLayout)
    }

    fun bindData(bio: String?, experienceYears: Int, radiusKm: Double) {
        etBio.setText(bio ?: "")
        etExperienceYears.setText(experienceYears.toString())
        val r = radiusKm.toFloat().coerceIn(5f, 100f)
        sliderRadius.value = r
        tvRadiusValue.text = "%.1f KM".format(r)
    }
}
