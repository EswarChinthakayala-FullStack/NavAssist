package com.navassist.android.presentation.assistant

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentWithdrawBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WithdrawFragment : BaseFragment<FragmentWithdrawBinding>(FragmentWithdrawBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnConfirmWithdraw.setOnClickListener {
            val amount = binding.etWithdrawAmount.text.toString().trim()
            if (amount.isEmpty()) {
                binding.tilWithdrawAmount.error = "Enter withdrawal amount"
            } else {
                showToast("Instant payout initiated to bank ✓")
                findNavController().navigateUp()
            }
        }
    }

    override fun observeViewModel() {}
}
