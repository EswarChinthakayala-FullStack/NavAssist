package com.navassist.android.presentation.admin.kyc

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.navassist.android.databinding.FragmentKycReviewDetailBinding
import com.navassist.android.presentation.admin.kyc.dialog.RejectReasonBottomSheet
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KycReviewDetailFragment : BaseFragment<FragmentKycReviewDetailBinding>(FragmentKycReviewDetailBinding::inflate) {

    private val viewModel: KycReviewDetailViewModel by viewModels()
    private var targetAssistantId: Int = 1

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
        targetAssistantId = arguments?.getInt("assistantId", 1) ?: 1
        viewModel.loadAssistantDetail(targetAssistantId)
    }

    private fun setupListeners() {
        // Approve Button
        binding.btnApproveKyc.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Approve Assistant KYC?")
                .setMessage("Are you sure you want to approve this assistant? They will immediately become eligible to accept passenger bookings.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Approve Now") { _, _ ->
                    binding.coordinatorLayout.announceForAccessibility("Approving assistant KYC")
                    viewModel.approveKyc(targetAssistantId)
                }
                .show()
        }

        // Reject Button
        binding.btnRejectKyc.setOnClickListener {
            val sheet = RejectReasonBottomSheet.newInstance("Applicant Guide") { reason ->
                binding.coordinatorLayout.announceForAccessibility("Rejecting assistant KYC")
                viewModel.rejectKyc(targetAssistantId, reason)
            }
            sheet.show(childFragmentManager, RejectReasonBottomSheet.TAG)
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

                                val item = state.data
                                val name = item.fullName ?: "Applicant #${item.id}"
                                binding.tvApplicantName.text = name
                                binding.tvApplicantMeta.text = "Assistant ID: #${item.id} • Status: ${item.verificationStatus}"

                                binding.docAadhaarFront.bindDocument("Aadhaar Card (Front)", item.aadhaarFrontUrl)
                                binding.docAadhaarBack.bindDocument("Aadhaar Card (Back)", item.aadhaarBackUrl)
                                binding.docProfilePhoto.bindDocument("Live Profile Photograph", null)

                                binding.trustScoreCard.bindMetrics(score = 98)
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
                            is KycDetailEffect.ShowToast -> showToast(effect.message)
                            is KycDetailEffect.ShowSnackbar -> showSnackbar(effect.message)
                            is KycDetailEffect.NavigateBack -> {
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }
}
