package com.navassist.android.presentation.emergency.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.navassist.android.R

class EmptyContactsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val btnAddFirstContact: MaterialButton
    var onAddClickListener: (() -> Unit)? = null

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val density = context.resources.displayMetrics.density
        val pad = (32 * density).toInt()
        setPadding(pad, pad, pad, pad)

        val ivShield = ImageView(context).apply {
            val size = (64 * density).toInt()
            layoutParams = LayoutParams(size, size).apply {
                bottomMargin = (16 * density).toInt()
            }
            setImageResource(R.drawable.ic_feature_safety)
            setColorFilter(Color.parseColor("#3F3F46"))
        }

        val tvHeading = TextView(context).apply {
            text = "No Emergency Contacts"
            textSize = 18f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val tvSub = TextView(context).apply {
            text = "Add trusted family or friends so they can be notified immediately if you trigger an SOS during a journey."
            textSize = 14f
            setTextColor(Color.parseColor("#71717A"))
            gravity = Gravity.CENTER
            setPadding(0, (6 * density).toInt(), 0, (20 * density).toInt())
        }

        btnAddFirstContact = MaterialButton(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, (48 * density).toInt())
            text = "+ Add First Emergency Contact"
            textSize = 14f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        btnAddFirstContact.setOnClickListener { onAddClickListener?.invoke() }

        addView(ivShield)
        addView(tvHeading)
        addView(tvSub)
        addView(btnAddFirstContact)
    }
}
