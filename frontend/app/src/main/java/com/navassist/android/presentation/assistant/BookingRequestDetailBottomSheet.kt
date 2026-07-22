package com.navassist.android.presentation.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.databinding.BottomSheetBookingRequestDetailBinding

class BookingRequestDetailBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBookingRequestDetailBinding? = null
    private val binding get() = _binding!!

    var onAcceptClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBookingRequestDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRejectRequest.setOnClickListener { dismiss() }
        binding.btnAcceptRequest.setOnClickListener {
            dismiss()
            onAcceptClicked?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BookingRequestDetailBottomSheet"
        fun newInstance(onAccept: () -> Unit) = BookingRequestDetailBottomSheet().apply {
            onAcceptClicked = onAccept
        }
    }
}
