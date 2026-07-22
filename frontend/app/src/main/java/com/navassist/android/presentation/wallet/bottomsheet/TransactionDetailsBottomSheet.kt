package com.navassist.android.presentation.wallet.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.navassist.android.domain.model.TransactionType
import com.navassist.android.domain.model.WalletTransaction

class TransactionDetailsBottomSheet : BottomSheetDialogFragment() {

    private var transaction: WalletTransaction? = null
    private var onDownloadReceipt: ((String) -> Unit)? = null

    companion object {
        const val TAG = "TransactionDetailsBottomSheet"
        fun newInstance(
            transaction: WalletTransaction,
            onDownloadReceipt: (String) -> Unit
        ): TransactionDetailsBottomSheet {
            return TransactionDetailsBottomSheet().apply {
                this.transaction = transaction
                this.onDownloadReceipt = onDownloadReceipt
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

        val item = transaction
        val isCredit = item?.type == TransactionType.CREDIT

        val tvTitle = TextView(requireContext()).apply {
            text = "Transaction Details"
            textSize = 20f
            setTextColor(Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvAmount = TextView(requireContext()).apply {
            text = if (isCredit) "+₹%.2f".format(item?.amount ?: 0.0) else "-₹%.2f".format(item?.amount ?: 0.0)
            textSize = 32f
            setTextColor(if (isCredit) Color.parseColor("#22C55E") else Color.parseColor("#FAFAFA"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, (8 * density).toInt(), 0, (16 * density).toInt())
        }

        val detailsCol = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111113"))
            val p = (14 * density).toInt()
            setPadding(p, p, p, p)
        }

        fun addRow(label: String, value: String) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
            }
            val tvL = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = label
                textSize = 13f
                setTextColor(Color.parseColor("#A1A1AA"))
            }
            val tvV = TextView(requireContext()).apply {
                text = value
                textSize = 13f
                setTextColor(Color.parseColor("#FAFAFA"))
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            row.addView(tvL); row.addView(tvV)
            detailsCol.addView(row)
        }

        addRow("Transaction Reference ID", item?.id ?: "TXN-90124")
        addRow("Type / Category", if (isCredit) "Wallet Credit / Top-Up" else "Booking Payment")
        addRow("Description", item?.description ?: "Payment via Razorpay Gateway")
        addRow("Timestamp", item?.timestamp ?: "22 Jul 2026, 02:30 PM")
        addRow("Gateway Status", item?.status ?: "SUCCESS")

        val btnReceipt = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (50 * density).toInt()
            ).apply {
                topMargin = (20 * density).toInt()
            }
            text = "Download Official Receipt (PDF)"
            textSize = 14f
            setTextColor(Color.parseColor("#FAFAFA"))
            setBackgroundColor(Color.parseColor("#27272A"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            setOnClickListener {
                dismiss()
                onDownloadReceipt?.invoke(item?.id ?: "")
            }
        }

        root.addView(tvTitle)
        root.addView(tvAmount)
        root.addView(detailsCol)
        root.addView(btnReceipt)

        return root
    }
}
