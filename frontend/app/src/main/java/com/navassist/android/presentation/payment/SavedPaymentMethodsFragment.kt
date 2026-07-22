package com.navassist.android.presentation.payment

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentSavedPaymentMethodsBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedPaymentMethodsFragment : BaseFragment<FragmentSavedPaymentMethodsBinding>(FragmentSavedPaymentMethodsBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddPaymentMethod.setOnClickListener {
            showToast("Opening Razorpay payment setup...")
        }
    }

    override fun observeViewModel() {}
}
