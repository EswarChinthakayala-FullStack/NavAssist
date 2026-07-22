package com.navassist.android.presentation.emergency.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class DeleteContactBottomSheet : BottomSheetDialogFragment() {

    private var contactName: String = "Contact"
    private var onConfirmDelete: (() -> Unit)? = null

    companion object {
        const val TAG = "DeleteContactBottomSheet"
        fun newInstance(name: String, onConfirm: () -> Unit): DeleteContactBottomSheet {
            return DeleteContactBottomSheet().apply {
                contactName = name
                onConfirmDelete = onConfirm
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
            text = "Delete Emergency Contact?"
            textSize = 20f
            setTextColor(Color.parseColor("#EF4444"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val tvSub = TextView(requireContext()).apply {
            text = "Are you sure you want to remove $contactName from your emergency safety contacts? They will no longer receive automated SOS alerts."
            textSize = 14f
            setTextColor(Color.parseColor("#A1A1AA"))
            setPadding(0, (6 * density).toInt(), 0, (20 * density).toInt())
        }

        val actionsRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val btnCancel = MaterialButton(requireContext(), null, com.google.android.material.R.attr.borderlessButtonStyle).apply {
            layoutParams = LinearLayout.LayoutParams(0, (50 * density).toInt(), 1f).apply {
                marginEnd = (8 * density).toInt()
            }
            text = "Cancel"
            setTextColor(Color.parseColor("#FAFAFA"))
            setOnClickListener { dismiss() }
        }

        val btnDelete = MaterialButton(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(0, (50 * density).toInt(), 1f).apply {
                marginStart = (8 * density).toInt()
            }
            text = "Remove Contact"
            textSize = 14f
            setTextColor(Color.parseColor("#FFFFFF"))
            setBackgroundColor(Color.parseColor("#EF4444"))
            cornerRadius = (14 * density).toInt()
            isAllCaps = false
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setOnClickListener {
                dismiss()
                onConfirmDelete?.invoke()
            }
        }

        actionsRow.addView(btnCancel)
        actionsRow.addView(btnDelete)

        root.addView(tvTitle)
        root.addView(tvSub)
        root.addView(actionsRow)

        return root
    }
}
