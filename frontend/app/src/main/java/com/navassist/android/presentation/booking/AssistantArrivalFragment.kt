package com.navassist.android.presentation.booking

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentAssistantArrivalBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssistantArrivalFragment : BaseFragment<FragmentAssistantArrivalBinding>(FragmentAssistantArrivalBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnVerifyOtp.setOnClickListener {
            val sheet = PickupOtpBottomSheet.newInstance()
            sheet.show(childFragmentManager, PickupOtpBottomSheet.TAG)
        }
    }

    override fun observeViewModel() {}
}
