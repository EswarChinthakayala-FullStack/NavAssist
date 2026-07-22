package com.navassist.android.presentation.wallet.bottomsheet

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
import com.navassist.android.R

class TopUpBottomSheet : BottomSheetDialogFragment() {

    private var onProceedTopUp: ((Double) -> Unit)? = null

    companion object {
        const val TAG = "TopUpBottomSheet"
        fun newInstance(onProceed: (Double) -> Unit): TopUpBottomSheet {
            return TopUpBottomSheet().apply {
                onProceedTopUp = onProceed
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
            text = "Add Money to Wallet"
            textSize = 20f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Select or enter an amount to top up using UPI, Cards, or Netbanking."
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val etAmount = EditText(requireContext()).apply {
            hint = "Enter Amount (e.g. ₹500)"
            textSize = 18f
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
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

        val amounts = listOf(100.0, 250.0, 500.0, 1000.0, 2000.0)
        amounts.forEach { amt ->
            val chip = Chip(requireContext()).apply {
                text = "₹${amt.toInt()}"
                isCheckable = true
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#27272A"))
                setTextColor(Color.parseColor("#FAFAFA"))
                setOnClickListener {
                    etAmount.setText(amt.toInt().toString())
                }
            }
            chipGroup.addView(chip)
        }

        val btnProceed = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (52 * density).toInt()
            )
            text = "Proceed with Razorpay"
            textSize = 15f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                val inputStr = etAmount.text.toString().trim()
                val valAmt = inputStr.toDoubleOrNull() ?: 0.0
                if (valAmt >= 10.0) {
                    dismiss()
                    onProceedTopUp?.invoke(valAmt)
                } else {
                    etAmount.error = "Minimum top up amount is ₹10"
                }
            }
        }

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(etAmount)
        root.addView(chipGroup)
        root.addView(btnProceed)

        return root
    }
}
