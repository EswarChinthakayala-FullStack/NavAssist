package com.navassist.android.presentation.assistant.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navassist.android.R
import com.navassist.android.databinding.FragmentAssistantHomeBinding
import com.navassist.android.presentation.assistant.home.adapter.IncomingBookingAdapter
import com.navassist.android.presentation.assistant.home.adapter.QuickActionAdapter
import com.navassist.android.presentation.assistant.home.adapter.QuickActionItem
import com.navassist.android.presentation.assistant.home.adapter.RecentTripsAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AssistantHomeFragment : BaseFragment<FragmentAssistantHomeBinding>(FragmentAssistantHomeBinding::inflate) {

    private val viewModel: AssistantHomeViewModel by viewModels()

    private lateinit var incomingBookingAdapter: IncomingBookingAdapter
    private lateinit var recentTripsAdapter: RecentTripsAdapter
    private lateinit var quickActionAdapter: QuickActionAdapter

    override fun setupViews() {
        setupAdapters()
        setupListeners()
    }

    private fun setupAdapters() {
        // Incoming Bookings Adapter
        incomingBookingAdapter = IncomingBookingAdapter(
            onAccept = { booking ->
                openBookingRequestBottomSheet(booking.id)
            },
            onDecline = { booking ->
                binding.coordinatorLayout.announceForAccessibility("Declined booking request")
                viewModel.rejectBooking(booking)
            }
        )
        binding.rvIncomingBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIncomingBookings.adapter = incomingBookingAdapter

        // Recent Trips Adapter
        recentTripsAdapter = RecentTripsAdapter { booking ->
            showToast("Trip details for ${booking.guestName}")
        }
        binding.rvRecentTrips.adapter = recentTripsAdapter

        // Quick Actions Shortcuts
        val quickActions = listOf(
            QuickActionItem("trips", "My Trips", R.drawable.ic_nav_bookings, R.id.tripHistoryFragment),
            QuickActionItem("earnings", "Earnings", R.drawable.ic_nav_wallet, R.id.walletFragment),
            QuickActionItem("wallet", "Wallet", R.drawable.ic_feature_payments, R.id.walletFragment),
            QuickActionItem("messages", "Messages", R.drawable.ic_nav_chat, R.id.chatFragment),
            QuickActionItem("nav", "Navigation", R.drawable.ic_onboarding_hero_2_navigation),
            QuickActionItem("profile", "Profile", R.drawable.ic_nav_profile, R.id.userProfileFragment)
        )

        quickActionAdapter = QuickActionAdapter(quickActions) { action ->
            action.destinationId?.let { dest ->
                try {
                    findNavController().navigate(dest)
                } catch (e: Exception) {
                    showToast("Opening ${action.title}")
                }
            } ?: run {
                showToast("Opening ${action.title}")
            }
        }
        binding.quickActionsCard.rvQuickActions.adapter = quickActionAdapter
    }

    private fun setupListeners() {
        // Online / Offline Switch listener
        binding.statusCard.statusSwitch.onStatusChangeListener = { isOnline ->
            val statusMessage = if (isOnline) "Switching status to Online" else "Switching status to Offline"
            binding.coordinatorLayout.announceForAccessibility(statusMessage)
            viewModel.toggleOnlineStatus(isOnline)
        }

        // Retry Button
        binding.btnRetry.setOnClickListener {
            binding.cardError.visibility = View.GONE
            viewModel.loadDashboard()
        }

        // Profile Completion / KYC Card Click Listener
        binding.completionCard.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_assistant_to_kyc)
            } catch (e: Exception) {
                showToast("Opening Identity Verification...")
            }
        }

        // Floating SOS Button
        binding.fabSos.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_assistant_to_sos)
            } catch (e: Exception) {
                showToast("Emergency SOS Triggered!")
            }
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI Profile State
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                                binding.cardError.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE
                                binding.cardError.visibility = View.GONE

                                val profile = state.data
                                val firstName = profile.name.split(" ").firstOrNull() ?: profile.name
                                binding.tvGreeting.text = "Good Morning, $firstName 👋"
                                binding.statusCard.bindProfile(profile)
                                binding.completionCard.setCompletion(profile.profileCompletionPct, profile.verificationStatus)
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                binding.cardError.visibility = View.VISIBLE
                                binding.tvErrorMessage.text = state.message
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Dashboard Stats
                launch {
                    viewModel.dashboardStats.collect { stats ->
                        binding.statsCard.bindStats(stats)
                    }
                }

                // Collect Today's Earnings
                launch {
                    viewModel.todayEarnings.collect { earnings ->
                        binding.earningsCard.bindEarnings(earnings)
                    }
                }

                // Collect Online State & Incoming Bookings
                launch {
                    viewModel.isOnline.collect { isOnline ->
                        if (isOnline) {
                            binding.emptyStateView.visibility = View.GONE
                        } else {
                            binding.emptyStateView.visibility = View.VISIBLE
                            binding.rvIncomingBookings.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.incomingBookings.collect { bookings ->
                        if (viewModel.isOnline.value) {
                            if (bookings.isEmpty()) {
                                binding.emptyStateView.visibility = View.VISIBLE
                                binding.rvIncomingBookings.visibility = View.GONE
                            } else {
                                binding.emptyStateView.visibility = View.GONE
                                binding.rvIncomingBookings.visibility = View.VISIBLE
                                incomingBookingAdapter.submitList(bookings)
                                binding.coordinatorLayout.announceForAccessibility("New incoming booking request available")
                            }
                        }
                    }
                }

                // Collect UI Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is AssistantHomeEffect.ShowToast -> showToast(effect.message)
                            is AssistantHomeEffect.ShowSnackbar -> showSnackbar(effect.message)
                            is AssistantHomeEffect.NavigateToTrip -> {
                                openBookingRequestBottomSheet(effect.bookingId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun openBookingRequestBottomSheet(bookingId: String) {
        if (childFragmentManager.findFragmentByTag(com.navassist.android.presentation.assistant.booking.BookingRequestBottomSheet.TAG) == null) {
            val bottomSheet = com.navassist.android.presentation.assistant.booking.BookingRequestBottomSheet.newInstance(bookingId)
            bottomSheet.show(childFragmentManager, com.navassist.android.presentation.assistant.booking.BookingRequestBottomSheet.TAG)
        }
    }
}
