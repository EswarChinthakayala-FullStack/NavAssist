package com.navassist.android.presentation.booking

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.navassist.android.R
import com.navassist.android.core.utils.CurrencyUtils
import com.navassist.android.databinding.FragmentBookingsBinding
import com.navassist.android.presentation.base.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookingsViewModel by viewModels()
    private lateinit var adapter: BookingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        setupSearchAndFilter()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = BookingsAdapter { booking ->
            val bundle = androidx.core.os.bundleOf("bookingId" to booking.id)
            findNavController().navigate(R.id.action_bookings_to_tripDetail, bundle)
        }

        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter
    }

    private fun setupClickListeners() {
        // Pull-to-Refresh SwipeRefreshLayout
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.color_primary,
            R.color.status_success
        )
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.color_surface)
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadBookings()
        }

        // Clear Search Button
        binding.ivClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        // Empty State CTA: Book Assistant
        binding.btnBookFirstAssistant.setOnClickListener {
            findNavController().navigate(R.id.action_bookings_to_bookAssistant)
        }
    }

    private fun setupSearchAndFilter() {
        // Search Input TextWatcher
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                binding.ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.setSearchQuery(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Status Filter Chips Listener
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipCompleted -> "COMPLETED"
                R.id.chipPending -> "PENDING"
                R.id.chipCancelled -> "CANCELLED"
                else -> "ALL"
            }
            viewModel.setStatusFilter(filter)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect statistics header summary metrics
                launch {
                    viewModel.stats.collect { stats ->
                        binding.tvStatTotalTrips.text = stats.totalTrips.toString()
                        binding.tvStatCompleted.text = stats.completedTrips.toString()
                        binding.tvStatPending.text = stats.pendingTrips.toString()
                        binding.tvStatTotalSpent.text = CurrencyUtils.formatInr(stats.amountSpent)
                    }
                }

                // Collect filtered bookings list
                launch {
                    viewModel.filteredBookingsState.collect { state ->
                        binding.swipeRefreshLayout.isRefreshing = false
                        when (state) {
                            is UiState.Idle -> binding.progressBar.visibility = View.GONE
                            is UiState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                                binding.layoutEmptyState.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                if (state.data.isEmpty()) {
                                    binding.rvBookings.visibility = View.GONE
                                    binding.layoutEmptyState.visibility = View.VISIBLE
                                } else {
                                    binding.layoutEmptyState.visibility = View.GONE
                                    binding.rvBookings.visibility = View.VISIBLE
                                    adapter.submitList(state.data)
                                }
                            }
                            is UiState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                binding.layoutEmptyState.visibility = View.VISIBLE
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
