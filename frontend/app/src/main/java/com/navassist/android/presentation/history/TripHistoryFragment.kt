package com.navassist.android.presentation.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentTripHistoryBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.history.adapter.TripHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TripHistoryFragment : BaseFragment<FragmentTripHistoryBinding>(FragmentTripHistoryBinding::inflate) {

    private val historyViewModel: TripHistoryViewModel by viewModels()
    private val tripAdapter = TripHistoryAdapter { trip ->
        showToast("Selected ${trip.bookingCode} (${trip.fare})")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.rvTripHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTripHistory.adapter = tripAdapter

        historyViewModel.loadTripHistory()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                historyViewModel.historyState.collect { state ->
                    if (state is UiState.Success) {
                        tripAdapter.submitList(state.data)
                    }
                }
            }
        }
    }
}
