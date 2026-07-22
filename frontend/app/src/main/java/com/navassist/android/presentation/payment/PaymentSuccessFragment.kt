package com.navassist.android.presentation.payment

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentPaymentSuccessBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentSuccessFragment : BaseFragment<FragmentPaymentSuccessBinding>(FragmentPaymentSuccessBinding::inflate) {

    override fun setupViews() {
        binding.btnContinueTracking.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    override fun observeViewModel() {}
}
