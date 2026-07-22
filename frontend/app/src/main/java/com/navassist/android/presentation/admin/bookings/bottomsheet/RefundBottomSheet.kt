package com.navassist.android.presentation.admin.bookings.bottomsheet

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

class RefundBottomSheet : BottomSheetDialogFragment() {

    private var bookingCode: String = "BK-1001"
    private var defaultAmount: Double = 250.0
    private var onConfirmRefund: ((Double, String) -> Unit)? = null

    companion object {
        const val TAG = "RefundBottomSheet"
        fun newInstance(code: String, fare: Double, onRefund: (Double, String) -> Unit): RefundBottomSheet {
            return RefundBottomSheet().apply {
                bookingCode = code
                defaultAmount = fare
                onConfirmRefund = onRefund
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
            text = "Issue Booking Refund"
            textSize = 20f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Process official refund to passenger's wallet or payment gateway for $bookingCode."
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val etAmount = EditText(requireContext()).apply {
            hint = "Refund Amount (₹)"
            setText(defaultAmount.toString())
            textSize = 16f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(Color.parseColor("#FAFAFA"))
            setHintTextColor(Color.parseColor("#71717A"))
            setBackgroundColor(Color.parseColor("#111113"))
            val p = (12 * density).toInt()
            setPadding(p, p, p, p)
        }

        val etReason = EditText(requireContext()).apply {
            hint = "Refund Reason..."
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
            "Duplicate Charge",
            "User Complaint",
            "Booking Failure",
            "Assistant Issue",
            "Admin Adjustment"
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

        val btnProcess = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (50 * density).toInt()
            )
            text = "Process Refund Now"
            textSize = 15f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                val amt = etAmount.text.toString().toDoubleOrNull() ?: defaultAmount
                val reason = etReason.text.toString().trim().ifEmpty { "Admin processed refund" }
                dismiss()
                onConfirmRefund?.invoke(amt, reason)
            }
        }

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(etAmount)
        root.addView(etReason)
        root.addView(chipGroup)
        root.addView(btnProcess)

        return root
    }
}
