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
import com.navassist.android.databinding.FragmentPriceEstimateBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PriceEstimateFragment : BaseFragment<FragmentPriceEstimateBinding>(FragmentPriceEstimateBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val estimateViewModel: PriceEstimateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.FARE)

        val pickup = bookingViewModel.pickupLocation.value
        val dest = bookingViewModel.destinationLocation.value

        val pLat = pickup?.latitude ?: 37.7749
        val pLng = pickup?.longitude ?: -122.4194
        val dLat = dest?.latitude ?: 37.7749
        val dLng = dest?.longitude ?: -122.4194

        estimateViewModel.loadFareEstimate(pLat, pLng, dLat, dLng)

        binding.cardCouponBanner.setOnClickListener {
            findNavController().navigate(R.id.action_estimate_to_offers)
        }

        binding.cardProceedCta.setOnClickListener {
            findNavController().navigate(R.id.action_estimate_to_pay)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                estimateViewModel.estimateState.collect { state ->
                    if (state is UiState.Success) {
                        val estimate = state.data
                        binding.cardTotalFare.setFare(estimate.totalFare, 8.4, 18)
                        binding.cardFareBreakdown.setFare(estimate.totalFare)
                        binding.cardSurgeIndicator.setMultiplier(estimate.surgeMultiplier)
                    }
                }
            }
        }
    }
}
