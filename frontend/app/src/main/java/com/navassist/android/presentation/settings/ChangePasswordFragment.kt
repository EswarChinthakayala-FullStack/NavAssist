package com.navassist.android.presentation.settings

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.databinding.FragmentChangePasswordBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>(FragmentChangePasswordBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdatePassword.setOnClickListener {
            val curr = binding.etCurrent.text.toString().trim()
            val newP = binding.etNew.text.toString().trim()

            if (curr.isEmpty()) {
                binding.tilCurrent.error = "Enter current password"
            } else if (newP.length < 6) {
                binding.tilNew.error = "Password must be at least 6 characters"
            } else {
                showToast("Password updated successfully ✓")
                findNavController().navigateUp()
            }
        }
    }

    override fun observeViewModel() {}
}
