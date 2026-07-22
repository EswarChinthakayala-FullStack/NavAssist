package com.navassist.android.presentation.admin.sos.bottomsheet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.navassist.android.data.remote.dto.sos.SosResponseDto

class SosDetailBottomSheet : BottomSheetDialogFragment() {

    private var alertDto: SosResponseDto? = null
    private var onResolve: ((Int) -> Unit)? = null

    companion object {
        const val TAG = "SosDetailBottomSheet"
        fun newInstance(dto: SosResponseDto, onResolveClick: (Int) -> Unit): SosDetailBottomSheet {
            return SosDetailBottomSheet().apply {
                alertDto = dto
                onResolve = onResolveClick
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

        val item = alertDto

        val tvTitle = TextView(requireContext()).apply {
            text = "Emergency Incident #${item?.id}"
            textSize = 20f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Live GPS: Lat ${item?.latitude}, Lng ${item?.longitude} • Status: ${item?.status}"
            textSize = 13f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (4 * density).toInt(), 0, (16 * density).toInt())
        }

        val colDetails = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111113"))
            val p = (14 * density).toInt()
            setPadding(p, p, p, p)
        }

        fun addRow(lbl: String, valStr: String) {
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, (4 * density).toInt(), 0, (4 * density).toInt())
            }
            val tvL = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = lbl; textSize = 13f; setTextColor(Color.parseColor("#A1A1AA"))
            }
            val tvV = TextView(requireContext()).apply {
                text = valStr; textSize = 13f; setTextColor(Color.parseColor("#FAFAFA")); typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            row.addView(tvL); row.addView(tvV)
            colDetails.addView(row)
        }

        addRow("Booking ID", "#${item?.bookingId ?: "N/A"}")
        addRow("Triggered By User ID", "#${item?.userId ?: "N/A"}")
        addRow("Triggered At", item?.triggeredAt ?: "22 Jul 2026, 02:30 PM")
        addRow("Current Incident Level", "CRITICAL 🔴")

        val btnResolve = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (50 * density).toInt()
            ).apply {
                topMargin = (20 * density).toInt()
            }
            text = "Resolve Emergency Incident ✓"
            textSize = 15f
            setTextColor(Color.parseColor("#09090B"))
            setBackgroundColor(Color.parseColor("#22C55E"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                dismiss()
                onResolve?.invoke(item?.id ?: 1)
            }
        }

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(colDetails)
        root.addView(btnResolve)

        return root
    }
}
