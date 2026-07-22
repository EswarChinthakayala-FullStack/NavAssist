package com.navassist.android.presentation.journey

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentGuestPickedUpBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GuestPickedUpFragment : BaseFragment<FragmentGuestPickedUpBinding>(FragmentGuestPickedUpBinding::inflate) {

    private val pickedUpViewModel: GuestPickedUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.cardOtpVerification.onVerifySubmit = { code ->
            pickedUpViewModel.verifyPickupOtp(10293, code)
        }

        binding.cardOtpVerification.onResendRequested = {
            pickedUpViewModel.resendPickupOtp(10293)
            showToast("New OTP sent to your registered mobile number.")
        }

        binding.cardVerifyCta.setOnClickListener {
            val code = binding.cardOtpVerification.otpInputView.getOtp()
            pickedUpViewModel.verifyPickupOtp(10293, code)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pickedUpViewModel.otpVerifyState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.cardVerifyCta.isEnabled = false
                            binding.tvVerifyCta.text = "Verifying Pickup..."
                        }
                        is UiState.Success -> {
                            binding.cardVerifyCta.isEnabled = true
                            binding.tvVerifyCta.text = "Pickup Verified!"
                            binding.cardOtpVerification.otpInputView.setSuccessState()
                            showToast("Pickup Verified Successfully! Journey Started.")

                            Handler(Looper.getMainLooper()).postDelayed({
                                if (isAdded) {
                                    findNavController().navigate(R.id.action_picked_up_to_navigation)
                                }
                            }, 1200)
                        }
                        is UiState.Error -> {
                            binding.cardVerifyCta.isEnabled = true
                            binding.tvVerifyCta.text = "Verify & Start Journey"
                            binding.cardOtpVerification.setErrorState()
                            showSnackbar(state.message)
                        }
                        else -> {
                            binding.cardVerifyCta.isEnabled = true
                            binding.tvVerifyCta.text = "Verify & Start Journey"
                        }
                    }
                }
            }
        }
    }
}
