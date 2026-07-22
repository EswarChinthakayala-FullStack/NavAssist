package com.navassist.android.presentation.journey

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentJourneyCompletedBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JourneyCompletedFragment : BaseFragment<FragmentJourneyCompletedBinding>(FragmentJourneyCompletedBinding::inflate) {

    private val completedViewModel: JourneyCompletedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        completedViewModel.loadCompletedTrip(10293)

        binding.btnRateAssistant.setOnClickListener {
            findNavController().navigate(R.id.action_completed_to_rate)
        }

        binding.btnBackHome.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                completedViewModel.tripState.collect { state ->
                    if (state is UiState.Success) {
                        val data = state.data
                        binding.tvFareAmount.text = data.fareTotal
                    }
                }
            }
        }
    }
}
