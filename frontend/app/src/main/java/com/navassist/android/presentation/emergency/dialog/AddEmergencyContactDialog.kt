package com.navassist.android.presentation.emergency.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class AddEmergencyContactDialog(
    context: Context,
    private val onAddContact: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

    private var selectedRelationship: String = "Guardian"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val density = context.resources.displayMetrics.density

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#09090B"))
            val pad = (24 * density).toInt()
            setPadding(pad, (40 * density).toInt(), pad, pad)
        }

        val tvTitle = TextView(context).apply {
            text = "Add Emergency Contact"
            textSize = 22f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(context).apply {
            text = "This contact will receive instant SMS & live location alerts if you trigger Emergency SOS."
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (4 * density).toInt(), 0, (20 * density).toInt())
        }

        // Full Name
        val tvNameLbl = TextView(context).apply { text = "Full Name *"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        val etName = EditText(context).apply {
            hint = "e.g. Rahul Sharma"
            textSize = 15f
            setTextColor(Color.parseColor("#FAFAFA"))
            setHintTextColor(Color.parseColor("#71717A"))
            setBackgroundColor(Color.parseColor("#18181B"))
            val p = (12 * density).toInt()
            setPadding(p, p, p, p)
        }

        // Phone Number
        val tvPhoneLbl = TextView(context).apply { text = "Phone Number (with Country Code) *"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")); typeface = android.graphics.Typeface.DEFAULT_BOLD; setPadding(0, (14 * density).toInt(), 0, 0) }
        val etPhone = EditText(context).apply {
            hint = "e.g. +919876543210"
            textSize = 15f
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setTextColor(Color.parseColor("#FAFAFA"))
            setHintTextColor(Color.parseColor("#71717A"))
            setBackgroundColor(Color.parseColor("#18181B"))
            val p = (12 * density).toInt()
            setPadding(p, p, p, p)
        }

        // Relationship Selector
        val tvRelLbl = TextView(context).apply { text = "Relationship *"; textSize = 12f; setTextColor(Color.parseColor("#A1A1AA")); typeface = android.graphics.Typeface.DEFAULT_BOLD; setPadding(0, (14 * density).toInt(), 0, 0) }
        val chipGroup = ChipGroup(context).apply {
            isSingleSelection = true
            setPadding(0, (6 * density).toInt(), 0, (16 * density).toInt())
        }

        val relationships = listOf("Father", "Mother", "Spouse", "Brother", "Sister", "Friend", "Relative", "Guardian", "Other")
        relationships.forEachIndexed { idx, rel ->
            val chip = Chip(context).apply {
                text = rel
                isCheckable = true
                isChecked = idx == 7 // Guardian default
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#18181B"))
                setTextColor(Color.parseColor("#FAFAFA"))
                setOnClickListener {
                    selectedRelationship = rel
                }
            }
            chipGroup.addView(chip)
        }

        // Primary Switch
        val primaryRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, (24 * density).toInt())
        }
        val tvPrimaryLbl = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            text = "Set as Primary Emergency Contact"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val switchPrimary = SwitchCompat(context).apply {
            isChecked = false
        }
        primaryRow.addView(tvPrimaryLbl)
        primaryRow.addView(switchPrimary)

        // Action Buttons
        val actionsRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btnCancel = MaterialButton(context, null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(0, (50 * density).toInt(), 1f).apply {
                marginEnd = (8 * density).toInt()
            }
            text = "Cancel"
            setTextColor(Color.parseColor("#FAFAFA"))
            setOnClickListener { dismiss() }
        }

        val btnSave = MaterialButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, (50 * density).toInt(), 2f).apply {
                marginStart = (8 * density).toInt()
            }
            text = "Save Contact"
            textSize = 15f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#FAFAFA"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                if (name.isBlank()) {
                    etName.error = "Name is required"
                    return@setOnClickListener
                }
                if (phone.length < 10) {
                    etPhone.error = "Valid phone number is required"
                    return@setOnClickListener
                }
                dismiss()
                onAddContact(name, phone, selectedRelationship, switchPrimary.isChecked)
            }
        }

        actionsRow.addView(btnCancel)
        actionsRow.addView(btnSave)

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(tvNameLbl)
        root.addView(etName)
        root.addView(tvPhoneLbl)
        root.addView(etPhone)
        root.addView(tvRelLbl)
        root.addView(chipGroup)
        root.addView(primaryRow)
        root.addView(actionsRow)

        setContentView(root)
    }
}
