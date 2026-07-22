package com.navassist.android.presentation.admin.users.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class SuspendUserBottomSheet : BottomSheetDialogFragment() {

    private var userName: String = "User"
    private var onConfirmSuspend: ((String) -> Unit)? = null

    companion object {
        const val TAG = "SuspendUserBottomSheet"
        fun newInstance(name: String, onSuspend: (String) -> Unit): SuspendUserBottomSheet {
            return SuspendUserBottomSheet().apply {
                userName = name
                onConfirmSuspend = onSuspend
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val density = requireContext().resources.displayMetrics.density
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#18181B"))
            val pad = (24 * density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val tvTitle = TextView(requireContext()).apply {
            text = "Suspend User Account"
            textSize = 20f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Are you sure you want to suspend $userName? Suspended users will be immediately logged out and blocked from booking services."
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val etReason = EditText(requireContext()).apply {
            hint = "Reason for suspension..."
            textSize = 14f
            minLines = 2
            setTextColor(Color.parseColor("#FAFAFA"))
            setHintTextColor(Color.parseColor("#71717A"))
            setBackgroundColor(Color.parseColor("#111113"))
            val p = (12 * density).toInt()
            setPadding(p, p, p, p)
        }

        val chipGroup = ChipGroup(requireContext()).apply {
            isSingleSelection = true
            setPadding(0, (12 * density).toInt(), 0, (20 * density).toInt())
        }

        val quickReasons = listOf(
            "Fraud / Security Alert",
            "Policy Violation",
            "Multiple Complaints",
            "Fake Account",
            "Admin Investigation"
        )
        quickReasons.forEach { reason ->
            val chip = Chip(requireContext()).apply {
                text = reason
                isCheckable = true
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
                setTextColor(Color.parseColor("#FAFAFA"))
                setOnClickListener {
                    etReason.setText(reason)
                }
            }
            chipGroup.addView(chip)
        }

        val btnSuspend = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (50 * density).toInt()
            )
            text = "Confirm Account Suspension"
            textSize = 15f
            setTextColor(Color.parseColor("#FFFFFF"))
            setBackgroundColor(Color.parseColor("#EF4444"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                val reason = etReason.text.toString().trim().ifEmpty { "Policy violation" }
                dismiss()
                onConfirmSuspend?.invoke(reason)
            }
        }

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(etReason)
        root.addView(chipGroup)
        root.addView(btnSuspend)

        return root
    }
}
