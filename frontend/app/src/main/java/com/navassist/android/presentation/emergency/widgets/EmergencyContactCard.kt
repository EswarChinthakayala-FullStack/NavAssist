package com.navassist.android.presentation.emergency.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.navassist.android.R
import com.navassist.android.domain.model.EmergencyContact

class EmergencyContactCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    val ivAvatar: ImageView
    val tvName: TextView
    val tvPhone: TextView
    val tvRelationship: TextView
    val badgePrimary: TextView
    val btnCall: MaterialButton
    val btnDelete: ImageView

    var onCallClickListener: (() -> Unit)? = null
    var onDeleteClickListener: (() -> Unit)? = null

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

        // Top Row
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        ivAvatar = ImageView(context).apply {
            val size = (44 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = (12 * density).toInt()
            }
            setImageResource(R.drawable.ic_role_passenger)
        }

        val colText = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        tvName = TextView(context).apply {
            text = "Contact Name"
            textSize = 16f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tvRelationship = TextView(context).apply {
            text = "Guardian • Emergency Contact"
            textSize = 12f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (2 * density).toInt(), 0, 0)
        }

        colText.addView(tvName)
        colText.addView(tvRelationship)

        btnDelete = ImageView(context).apply {
            val size = (24 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginStart = (8 * density).toInt()
            }
            setImageResource(R.drawable.ic_close)
            setColorFilter(Color.parseColor("#71717A"))
        }

        topRow.addView(ivAvatar)
        topRow.addView(colText)
        topRow.addView(btnDelete)

        // Middle Row (Phone & Primary Badge)
        val midRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, (12 * density).toInt(), 0, (12 * density).toInt())
        }

        tvPhone = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "+91 98765 43210"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.MONOSPACE
        }

        badgePrimary = TextView(context).apply {
            text = "PRIMARY ★"
            textSize = 10f
            setTextColor(Color.parseColor("#22C55E"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setBackgroundColor(Color.parseColor("#2622C55E"))
            val pV = (3 * density).toInt()
            val pH = (8 * density).toInt()
            setPadding(pH, pV, pH, pV)
        }

        midRow.addView(tvPhone)
        midRow.addView(badgePrimary)

        // Call Action
        btnCall = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (40 * density).toInt())
            text = "📞 Direct Call Contact"
            textSize = 12f
            setTextColor(Color.parseColor("#FAFAFA"))
            setBackgroundColor(Color.parseColor("#27272A"))
            cornerRadius = (12 * density).toInt()
            isAllCaps = false
        }

        btnCall.setOnClickListener { onCallClickListener?.invoke() }
        btnDelete.setOnClickListener { onDeleteClickListener?.invoke() }

        rootLayout.addView(topRow)
        rootLayout.addView(midRow)
        rootLayout.addView(btnCall)

        addView(rootLayout)
    }

    fun bindContact(contact: EmergencyContact) {
        tvName.text = contact.name
        tvPhone.text = contact.phone
        tvRelationship.text = "${contact.relationship} • SOS Recipient"

        if (contact.isPrimary) {
            badgePrimary.visibility = VISIBLE
            strokeColor = Color.parseColor("#22C55E")
        } else {
            badgePrimary.visibility = GONE
            strokeColor = Color.parseColor("#27272A")
        }
    }
}
