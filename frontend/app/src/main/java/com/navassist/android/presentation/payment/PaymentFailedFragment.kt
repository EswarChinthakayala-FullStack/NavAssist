package com.navassist.android.presentation.payment

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentPaymentFailedBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentFailedFragment : BaseFragment<FragmentPaymentFailedBinding>(FragmentPaymentFailedBinding::inflate) {

    override fun setupViews() {
        binding.btnRetryPayment.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun observeViewModel() {}
}
