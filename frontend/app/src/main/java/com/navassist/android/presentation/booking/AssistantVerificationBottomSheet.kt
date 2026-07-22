package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.navassist.android.R
import com.navassist.android.databinding.BottomSheetAssistantVerificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssistantVerificationBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAssistantVerificationBinding? = null
    private val binding get() = _binding!!

    private val bookingViewModel: BookingViewModel by activityViewModels()

    override fun getTheme(): Int = com.google.android.material.R.style.Theme_Design_BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAssistantVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gaugeTrustScore.setScore(96)

        binding.cardAadhaarVerified.setVerification(
            "Aadhaar & Government ID Verified",
            "Biometric & identity record check completed"
        )

        binding.cardBackgroundCheck.setVerification(
            "Criminal Background Check Passed",
            "Zero criminal history verified by partner law enforcement"
        )

        binding.cardGotItCta.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AssistantVerificationBottomSheet"

        fun newInstance(): AssistantVerificationBottomSheet {
            return AssistantVerificationBottomSheet()
        }
    }
}
