package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentPaymentMethodBinding
import com.navassist.android.domain.model.PaymentMethod
import com.navassist.android.presentation.booking.adapter.PaymentMethodAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentMethodFragment : BaseFragment<FragmentPaymentMethodBinding>(FragmentPaymentMethodBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val paymentViewModel: PaymentMethodViewModel by viewModels()

    private lateinit var paymentAdapter: PaymentMethodAdapter
    private var selectedMethodId: String = "upi"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.PAYMENT)

        setupRecyclerView()

        binding.cardPayCta.setOnClickListener {
            paymentViewModel.processPayment(selectedMethodId)
        }
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentMethodAdapter { selectedMethod ->
            selectedMethodId = selectedMethod.id
        }
        binding.rvPaymentMethods.adapter = paymentAdapter

        val methods = listOf(
            PaymentMethod("upi", "UPI (Google Pay / PhonePe / Paytm)", "Instant bank transfer via any UPI app", R.drawable.ic_feature_tracking, isRecommended = true),
            PaymentMethod("card", "Credit / Debit Card", "Visa, Mastercard, RuPay & Maestro", R.drawable.ic_benefit_pickup),
            PaymentMethod("wallet", "NavAssist Wallet Balance", "Use available wallet credits", R.drawable.ic_benefit_safety),
            PaymentMethod("netbanking", "Net Banking", "All major Indian banks supported", R.drawable.ic_feature_tracking)
        )
        paymentAdapter.submitList(methods)
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Wallet Balance
                launch {
                    paymentViewModel.walletBalanceState.collect { state ->
                        if (state is UiState.Success) {
                            binding.cardWalletBalance.setBalance(state.data)
                        }
                    }
                }

                // Collect Payment Verification State
                launch {
                    paymentViewModel.paymentState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.cardPayCta.isEnabled = false
                                binding.tvPayCta.text = "Securing Order..."
                            }
                            is UiState.Success -> {
                                binding.cardPayCta.isEnabled = true
                                findNavController().navigate(R.id.action_payment_to_confirm)
                            }
                            is UiState.Error -> {
                                binding.cardPayCta.isEnabled = true
                                binding.tvPayCta.text = "Continue to Payment"
                                showSnackbar(state.message)
                            }
                            else -> {
                                binding.cardPayCta.isEnabled = true
                                binding.tvPayCta.text = "Continue to Payment"
                            }
                        }
                    }
                }
            }
        }
    }
}
