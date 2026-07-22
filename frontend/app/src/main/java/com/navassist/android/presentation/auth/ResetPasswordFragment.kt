package com.navassist.android.presentation.auth

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentResetPasswordBinding
import com.navassist.android.presentation.common.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResetPasswordFragment : BaseFragment<FragmentResetPasswordBinding>(FragmentResetPasswordBinding::inflate) {

    override fun setupViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnResetPassword.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            val pwd = binding.etNewPassword.text.toString().trim()

            if (otp.length < 6) {
                binding.tilOtp.error = "Enter 6-digit OTP code"
            } else if (pwd.length < 6) {
                binding.tilNewPassword.error = "Password must be at least 6 characters"
            } else {
                showToast("Password reset successfully ✓")
                findNavController().popBackStack(R.id.loginFragment, false)
            }
        }
    }

    override fun observeViewModel() {}
}
