package com.navassist.android.presentation.payment

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentPaymentProcessingBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentProcessingFragment : BaseFragment<FragmentPaymentProcessingBinding>(FragmentPaymentProcessingBinding::inflate) {

    override fun setupViews() {
        binding.root.postDelayed({
            if (isAdded) {
                findNavController().navigate(R.id.paymentSuccessFragment)
            }
        }, 2000)
    }

    override fun observeViewModel() {}
}
