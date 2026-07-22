package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.databinding.BottomSheetCancelBookingBinding

class CancelBookingBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCancelBookingBinding? = null
    private val binding get() = _binding!!

    var onCancelConfirmed: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetCancelBookingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirmCancel.setOnClickListener {
            val selectedId = binding.rgReasons.checkedRadioButtonId
            val reason = when (selectedId) {
                binding.rb2.id -> "Assistant is taking too long"
                binding.rb3.id -> "Wrong pickup or destination"
                binding.rb4.id -> "Booked by mistake"
                else -> "Changed mind"
            }
            dismiss()
            onCancelConfirmed?.invoke(reason)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CancelBookingBottomSheet"
        fun newInstance(onCancel: (String) -> Unit): CancelBookingBottomSheet {
            return CancelBookingBottomSheet().apply {
                onCancelConfirmed = onCancel
            }
        }
    }
}
