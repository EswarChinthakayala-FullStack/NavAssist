package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentBookingSummaryBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingSummaryFragment : BaseFragment<FragmentBookingSummaryBinding>(FragmentBookingSummaryBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.FARE)

        val pickup = bookingViewModel.pickupLocation.value?.address ?: "Pickup Location"
        val destination = bookingViewModel.destinationLocation.value?.address ?: "Destination Location"
        val scheduledAt = bookingViewModel.scheduledAt.value
        val assistant = bookingViewModel.selectedAssistant.value

        binding.cardRoutePreview.setRoute(pickup, destination, 8.4, 18)
        binding.cardAssistantPreview.setAssistant(assistant)
        binding.cardSchedulePreview.setScheduledTimestamp(scheduledAt)
        binding.cardFareSummary.setFare(240.0)

        binding.cardConfirmCta.setOnClickListener {
            bookingViewModel.createBooking()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bookingViewModel.bookingState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.cardConfirmCta.isEnabled = false
                            binding.tvConfirmCta.text = "Creating Booking..."
                        }
                        is UiState.Success -> {
                            binding.cardConfirmCta.isEnabled = true
                            findNavController().navigate(R.id.action_summary_to_payment)
                        }
                        is UiState.Error -> {
                            binding.cardConfirmCta.isEnabled = true
                            binding.tvConfirmCta.text = "Confirm Booking"
                            showSnackbar(state.message)
                        }
                        else -> {
                            binding.cardConfirmCta.isEnabled = true
                            binding.tvConfirmCta.text = "Confirm Booking"
                        }
                    }
                }
            }
        }
    }
}
