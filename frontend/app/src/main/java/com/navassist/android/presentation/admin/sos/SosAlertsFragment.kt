package com.navassist.android.presentation.admin.sos

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
import com.navassist.android.databinding.FragmentSosAlertsBinding
import com.navassist.android.presentation.admin.sos.adapter.SosAlertsAdapter
import com.navassist.android.presentation.admin.sos.bottomsheet.SosDetailBottomSheet
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SosAlertsFragment : BaseFragment<FragmentSosAlertsBinding>(FragmentSosAlertsBinding::inflate) {

    private val viewModel: SosAlertsViewModel by viewModels()
    private lateinit var sosAdapter: SosAlertsAdapter

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
        sosAdapter = SosAlertsAdapter(
            onResolveClick = { alert ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Resolve SOS Alert #${alert.id}?")
                    .setMessage("Are you sure you want to mark this emergency alert as resolved?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Resolve Incident") { _, _ ->
                        binding.coordinatorLayout.announceForAccessibility("Resolving SOS incident ${alert.id}")
                        viewModel.resolveAlert(alert.id)
                    }
                    .show()
            },
            onItemClick = { alert ->
                val sheet = SosDetailBottomSheet.newInstance(alert) { sosId ->
                    viewModel.resolveAlert(sosId)
                }
                sheet.show(childFragmentManager, SosDetailBottomSheet.TAG)
            }
        )

        binding.rvSosAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSosAlerts.adapter = sosAdapter
    }

    private fun setupListeners() {
        // Search Input Watcher
        binding.etSearchSos.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterAlerts(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // FAB Refresh
        binding.fabRefreshSos.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Refreshing live active SOS alerts")
            showToast("Refreshing active SOS alerts...")
            viewModel.loadActiveAlerts()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Alerts State
                launch {
                    viewModel.alertsState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val list = state.data
                                binding.tvActiveAlertCount.text = "ACTIVE EMERGENCY ALERTS: ${list.size} 🔴"

                                if (list.isEmpty()) {
                                    binding.tvEmptySos.visibility = View.VISIBLE
                                    binding.rvSosAlerts.visibility = View.GONE
                                } else {
                                    binding.tvEmptySos.visibility = View.GONE
                                    binding.rvSosAlerts.visibility = View.VISIBLE
                                    sosAdapter.submitList(list)
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
                            is SosAlertsEffect.ShowToast -> showToast(effect.message)
                            is SosAlertsEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
