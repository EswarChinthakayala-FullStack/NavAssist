package com.navassist.android.presentation.admin.kyc

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.navassist.android.R
import com.navassist.android.databinding.FragmentKycReviewQueueBinding
import com.navassist.android.presentation.admin.kyc.adapter.PendingKycAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class KycReviewQueueFragment : BaseFragment<FragmentKycReviewQueueBinding>(FragmentKycReviewQueueBinding::inflate) {

    private val viewModel: KycReviewQueueViewModel by viewModels()
    private lateinit var kycAdapter: PendingKycAdapter

    override fun setupViews() {
        setupToolbar()
        setupAdapter()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupAdapter() {
        kycAdapter = PendingKycAdapter(
            onApproveClick = { item ->
                val name = item.fullName ?: "Applicant #${item.id}"
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Approve Assistant KYC?")
                    .setMessage("Are you sure you want to approve $name? They will be granted full Assistant status to accept passenger bookings.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Approve") { _, _ ->
                        binding.coordinatorLayout.announceForAccessibility("Approving $name")
                        viewModel.approveKyc(item.userId, name)
                    }
                    .show()
            },
            onRejectClick = { item ->
                val name = item.fullName ?: "Applicant #${item.id}"
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Reject Assistant KYC?")
                    .setMessage("Specify reason or confirm rejection for $name.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Reject Application") { _, _ ->
                        binding.coordinatorLayout.announceForAccessibility("Rejecting $name")
                        viewModel.rejectKyc(item.userId, name, "Documents unreadable or incomplete")
                    }
                    .show()
            },
            onItemClick = { item ->
                val bundle = Bundle().apply { putInt("assistantId", item.userId) }
                findNavController().navigate(R.id.action_kyc_queue_to_detail, bundle)
            }
        )

        binding.rvPendingKyc.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingKyc.adapter = kycAdapter
    }

    private fun setupListeners() {
        // Search Input Watcher
        binding.etSearchKyc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterQueue(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Empty State Refresh Button
        binding.emptyStateView.onRefreshClickListener = {
            viewModel.loadPendingQueue()
        }

        // FAB Refresh
        binding.fabRefreshKyc.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Refreshing pending KYC review queue")
            showToast("Refreshing KYC review queue...")
            viewModel.loadPendingQueue()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Queue State
                launch {
                    viewModel.queueState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val list = state.data
                                binding.statsCard.bindStats(pending = list.size)

                                if (list.isEmpty()) {
                                    binding.emptyStateView.visibility = View.VISIBLE
                                    binding.rvPendingKyc.visibility = View.GONE
                                } else {
                                    binding.emptyStateView.visibility = View.GONE
                                    binding.rvPendingKyc.visibility = View.VISIBLE
                                    kycAdapter.submitList(list)
                                }
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
                            is KycQueueEffect.ShowToast -> showToast(effect.message)
                            is KycQueueEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
