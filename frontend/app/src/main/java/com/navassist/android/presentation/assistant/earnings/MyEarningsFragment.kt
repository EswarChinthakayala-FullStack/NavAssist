package com.navassist.android.presentation.assistant.earnings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navassist.android.R
import com.navassist.android.databinding.FragmentMyEarningsBinding
import com.navassist.android.presentation.assistant.earnings.adapter.EarningsHistoryAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyEarningsFragment : BaseFragment<FragmentMyEarningsBinding>(FragmentMyEarningsBinding::inflate) {

    private val viewModel: MyEarningsViewModel by viewModels()
    private lateinit var historyAdapter: EarningsHistoryAdapter

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
        historyAdapter = EarningsHistoryAdapter { item ->
            showToast("Trip #${item.bookingId}: Net ₹${item.netEarnings.toInt()}")
        }
        binding.rvEarningsHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEarningsHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        // Filter Chips
        binding.chipToday.setOnClickListener { viewModel.setFilterPeriod("today") }
        binding.chipThisWeek.setOnClickListener { viewModel.setFilterPeriod("this_week") }
        binding.chipThisMonth.setOnClickListener { viewModel.setFilterPeriod("this_month") }
        binding.chipLifetime.setOnClickListener { viewModel.setFilterPeriod("lifetime") }

        // Download Statement FAB
        binding.fabDownloadStatement.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Downloading earnings statement PDF")
            viewModel.downloadStatement()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Financial Dashboard State
                launch {
                    viewModel.earningsState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val data = state.data
                                binding.heroSummaryCard.bindSummary(
                                    data.lifetimeEarningsInr,
                                    data.todayEarningsInr,
                                    data.weeklyEarningsInr
                                )

                                binding.payoutCard.bindPayout(
                                    data.nextPayoutAmountInr,
                                    data.nextPayoutDate.ifBlank { "Sunday, 26 Jul 2026" },
                                    data.payoutStatus
                                )

                                binding.weeklyProgressCard.setProgress(data.weeklyEarningsInr, 5000.0)
                                binding.bonusCard.bindRewards(data.incentivesEarnedInr, data.bonusesEarnedInr)

                                if (data.earningsHistory.isEmpty()) {
                                    binding.emptyEarningsView.visibility = View.VISIBLE
                                    binding.rvEarningsHistory.visibility = View.GONE
                                } else {
                                    binding.emptyEarningsView.visibility = View.GONE
                                    binding.rvEarningsHistory.visibility = View.VISIBLE
                                    historyAdapter.submitList(data.earningsHistory)
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
                            is MyEarningsEffect.ShowToast -> showToast(effect.message)
                            is MyEarningsEffect.ShowSnackbar -> showSnackbar(effect.message)
                            is MyEarningsEffect.OpenStatementPdf -> {
                                showToast("Opening PDF statement...")
                            }
                        }
                    }
                }
            }
        }
    }
}
