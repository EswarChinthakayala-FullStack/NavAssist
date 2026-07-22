package com.navassist.android.presentation.booking

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentPaymentConfirmationBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentConfirmationFragment : BaseFragment<FragmentPaymentConfirmationBinding>(FragmentPaymentConfirmationBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val confirmationViewModel: PaymentConfirmationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.CONFIRM)

        confirmationViewModel.verifyPaymentAndFetchReceipt("BK_10293", "TXN_987654", "pay_8849201")

        binding.cardContinueCta.setOnClickListener {
            navigateToHomeAndClearBackstack()
        }
    }

    private fun navigateToHomeAndClearBackstack() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.bookingsFragment, true)
            .build()
        findNavController().navigate(R.id.action_confirm_to_assigned, null, navOptions)
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                confirmationViewModel.verificationState.collect { state ->
                    if (state is UiState.Success) {
                        val result = state.data
                        binding.cardBookingReceipt.setReceiptDetails(result.bookingId, result.transactionId, result.paymentId)
                        binding.cardFareSummary.setFare(result.amountPaid)
                        binding.viewSuccessAnim.playSuccessAnimation()

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isAdded) {
                                navigateToHomeAndClearBackstack()
                            }
                        }, 2500)
                    }
                }
            }
        }
    }
}
