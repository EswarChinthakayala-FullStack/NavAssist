package com.navassist.android.presentation.admin.kyc.dialog

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

class RejectReasonBottomSheet : BottomSheetDialogFragment() {

    private var assistantName: String = "Applicant"
    private var onConfirmReject: ((String) -> Unit)? = null

    companion object {
        const val TAG = "RejectReasonBottomSheet"
        fun newInstance(name: String, onReject: (String) -> Unit): RejectReasonBottomSheet {
            return RejectReasonBottomSheet().apply {
                assistantName = name
                onConfirmReject = onReject
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
            text = "Reject KYC Application"
            textSize = 20f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Select or provide an official reason for rejecting $assistantName's KYC verification. The applicant will be notified to re-upload."
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val etReason = EditText(requireContext()).apply {
            hint = "Enter detailed rejection reason (min 20 characters)..."
            textSize = 14f
            minLines = 3
            gravity = android.view.Gravity.TOP or android.view.Gravity.START
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
            "Blurry Document",
            "Invalid Aadhaar Number",
            "Mismatched Photo",
            "Incomplete Documents",
            "Duplicate Submission",
            "Incorrect Information"
        )
        quickReasons.forEach { reason ->
            val chip = Chip(requireContext()).apply {
                text = reason
                isCheckable = true
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
                setTextColor(Color.parseColor("#FAFAFA"))
                setOnClickListener {
                    etReason.setText("KYC Application Rejected: $reason. Please re-upload clear and valid identity documents.")
                }
            }
            chipGroup.addView(chip)
        }

        val btnConfirm = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (50 * density).toInt()
            )
            text = "Confirm Rejection"
            textSize = 15f
            setTextColor(Color.parseColor("#FFFFFF"))
            setBackgroundColor(Color.parseColor("#EF4444"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                val inputReason = etReason.text.toString().trim()
                if (inputReason.length < 15) {
                    etReason.error = "Please provide a reason with at least 15 characters"
                } else {
                    dismiss()
                    onConfirmReject?.invoke(inputReason)
                }
            }
        }

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(etReason)
        root.addView(chipGroup)
        root.addView(btnConfirm)

        return root
    }
}
