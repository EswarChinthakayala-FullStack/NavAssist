package com.navassist.android.presentation.admin.bookings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.navassist.android.databinding.FragmentAdminBookingDetailBinding
import com.navassist.android.presentation.admin.bookings.bottomsheet.RefundBottomSheet
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminBookingDetailFragment : BaseFragment<FragmentAdminBookingDetailBinding>(FragmentAdminBookingDetailBinding::inflate) {

    private val viewModel: AdminBookingDetailViewModel by viewModels()
    private var targetBookingId: Int = 1

    override fun setupViews() {
        setupToolbar()
        extractArgs()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun extractArgs() {
        targetBookingId = arguments?.getInt("bookingId", 1) ?: 1
        viewModel.loadBookingDetail(targetBookingId)
    }

    private fun setupListeners() {
        binding.btnIssueRefund.setOnClickListener {
            val sheet = RefundBottomSheet.newInstance("BK-#$targetBookingId", 250.0) { amount, reason ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Confirm Admin Refund?")
                    .setMessage("Are you sure you want to issue a refund of ₹%.2f for booking BK-#$targetBookingId? Reason: $reason".format(amount))
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Process Refund") { _, _ ->
                        binding.coordinatorLayout.announceForAccessibility("Processing refund for booking BK-#$targetBookingId")
                        viewModel.processRefund(targetBookingId, amount, reason)
                    }
                    .show()
            }
            sheet.show(childFragmentManager, RefundBottomSheet.TAG)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Detail State
                launch {
                    viewModel.detailState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val booking = state.data
                                binding.tvBookingCode.text = booking.bookingCode
                                binding.tvStatusBadge.text = booking.status.uppercase()
                                binding.tvPickupAddress.text = "Pickup: ${booking.pickupAddress}"
                                binding.tvDestinationAddress.text = "Destination: ${booking.destinationAddress}"
                                binding.tvGuestInfo.text = "Passenger ID: #${booking.guestId}"
                                binding.tvAssistantInfo.text = "Assistant ID: #${booking.assistantId ?: "Unassigned"}"
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                showSnackbar(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is AdminBookingDetailEffect.ShowToast -> showToast(effect.message)
                            is AdminBookingDetailEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
