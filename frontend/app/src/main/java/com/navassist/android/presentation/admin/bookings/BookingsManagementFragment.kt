package com.navassist.android.presentation.admin.bookings

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
import com.navassist.android.R
import com.navassist.android.databinding.FragmentBookingsManagementBinding
import com.navassist.android.presentation.admin.bookings.adapter.AdminBookingsAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingsManagementFragment : BaseFragment<FragmentBookingsManagementBinding>(FragmentBookingsManagementBinding::inflate) {

    private val viewModel: BookingsManagementViewModel by viewModels()
    private lateinit var bookingsAdapter: AdminBookingsAdapter

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
        bookingsAdapter = AdminBookingsAdapter { booking ->
            val bundle = Bundle().apply { putInt("bookingId", booking.id) }
            findNavController().navigate(R.id.action_admin_bookings_to_detail, bundle)
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = bookingsAdapter
    }

    private fun setupListeners() {
        // Search Input Watcher
        binding.etSearchBooking.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterBookings(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // FAB Refresh
        binding.fabRefreshBookings.setOnClickListener {
            binding.coordinatorLayout.announceForAccessibility("Refreshing bookings management queue")
            showToast("Refreshing bookings queue...")
            viewModel.loadBookings()
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Bookings State
                launch {
                    viewModel.bookingsState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutContent.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutContent.visibility = View.VISIBLE

                                val list = state.data
                                if (list.isEmpty()) {
                                    binding.tvEmptyBookings.visibility = View.VISIBLE
                                    binding.rvBookings.visibility = View.GONE
                                } else {
                                    binding.tvEmptyBookings.visibility = View.GONE
                                    binding.rvBookings.visibility = View.VISIBLE
                                    bookingsAdapter.submitList(list)
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
                            is BookingsEffect.ShowToast -> showToast(effect.message)
                            is BookingsEffect.ShowSnackbar -> showSnackbar(effect.message)
                        }
                    }
                }
            }
        }
    }
}
