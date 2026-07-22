package com.navassist.android.presentation.admin

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentAdminDashboardBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AdminDashboardFragment : BaseFragment<FragmentAdminDashboardBinding>(FragmentAdminDashboardBinding::inflate) {

    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun setupViews() {
        setupToolbar()
        setupActionCards()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupActionCards() {
        binding.actionKycQueue.bindAction(
            iconRes = R.drawable.ic_nav_profile,
            title = "Assistant KYC Verification Queue",
            badgeText = "Review Applications",
            badgeColorHex = "#F59E0B"
        )
        binding.actionUserManagement.bindAction(
            iconRes = R.drawable.ic_person_outline,
            title = "User & Assistant Accounts",
            badgeText = "Active Directory",
            badgeColorHex = "#22C55E"
        )
        binding.actionBookingsManagement.bindAction(
            iconRes = R.drawable.ic_feature_booking,
            title = "Bookings & Journeys Queue",
            badgeText = "Full History",
            badgeColorHex = "#FAFAFA"
        )
        binding.actionSosMonitor.bindAction(
            iconRes = R.drawable.ic_feature_safety,
            title = "Live Emergency SOS Monitor",
            badgeText = "System Shield",
            badgeColorHex = "#EF4444"
        )
    }

    private fun setupListeners() {
        binding.actionKycQueue.onActionClickListener = {
            viewModel.onKycQueueClicked()
        }

        binding.actionUserManagement.onActionClickListener = {
            viewModel.onUsersClicked()
        }

        binding.actionBookingsManagement.onActionClickListener = {
            viewModel.onBookingsClicked()
        }

        binding.actionSosMonitor.onActionClickListener = {
            viewModel.onSosClicked()
        }

        binding.fabRefreshAdmin.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Refreshing enterprise admin console metrics")
            showToast("Refreshing enterprise admin stats...")
            viewModel.loadDashboardStats()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Stats State
                launch {
                    viewModel.statsState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val stats = state.data
                                binding.statRegisteredUsers.bindStat(
                                    title = "Registered Users",
                                    value = stats.totalRegisteredUsers,
                                    subText = "+12% this month",
                                    accentHex = "#FAFAFA"
                                )
                                binding.statTotalBookings.bindStat(
                                    title = "Bookings Processed",
                                    value = stats.totalBookingsProcessed,
                                    subText = "+8.4% this week",
                                    accentHex = "#FAFAFA"
                                )
                                binding.statPendingKyc.bindStat(
                                    title = "Pending KYC Reviews",
                                    value = stats.pendingKycReviews,
                                    subText = "Requires Admin Action",
                                    accentHex = "#F59E0B"
                                )
                                binding.statOpenTickets.bindStat(
                                    title = "Open Support Tickets",
                                    value = stats.openTicketsCount,
                                    subText = "Customer Care Queue",
                                    accentHex = "#3B82F6"
                                )
                                binding.statPlatformRevenue.bindAmount(
                                    title = "TOTAL PLATFORM REVENUE",
                                    amount = 148500.0,
                                    subText = "+18.5% growth vs last month",
                                    accentHex = "#22C55E"
                                )
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
                            is AdminEffect.ShowToast -> showToast(effect.message)
                            is AdminEffect.ShowSnackbar -> showSnackbar(effect.message)
                            is AdminEffect.NavigateToKycQueue -> {
                                findNavController().navigate(R.id.kycReviewQueueFragment)
                            }
                            is AdminEffect.NavigateToUsers -> {
                                findNavController().navigate(R.id.userManagementFragment)
                            }
                            is AdminEffect.NavigateToBookings -> {
                                findNavController().navigate(R.id.bookingsManagementFragment)
                            }
                            is AdminEffect.NavigateToSos -> {
                                findNavController().navigate(R.id.sosAlertsFragment)
                            }
                        }
                    }
                }
            }
        }
    }
}
