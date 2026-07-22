package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentAvailableAssistantsBinding
import com.navassist.android.presentation.booking.adapter.AssistantListAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AvailableAssistantsFragment : BaseFragment<FragmentAvailableAssistantsBinding>(FragmentAvailableAssistantsBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val assistantsViewModel: AvailableAssistantsViewModel by viewModels()

    private lateinit var assistantAdapter: AssistantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.ASSISTANT)

        setupRecyclerView()

        val pickup = bookingViewModel.pickupLocation.value
        val lat = pickup?.latitude ?: 37.7749
        val lng = pickup?.longitude ?: -122.4194
        assistantsViewModel.loadNearbyAssistants(lat, lng)
    }

    private fun setupRecyclerView() {
        assistantAdapter = AssistantListAdapter { selectedAssistant ->
            bookingViewModel.selectAssistant(selectedAssistant)
            findNavController().navigate(R.id.action_assistants_to_profile)
        }
        binding.rvAssistants.adapter = assistantAdapter
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                assistantsViewModel.assistantsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.layoutSkeleton.visibility = View.VISIBLE
                            binding.rvAssistants.visibility = View.GONE
                            binding.tvAssistantCount.text = "Searching for assistants nearby…"
                        }
                        is UiState.Success -> {
                            binding.layoutSkeleton.visibility = View.GONE
                            binding.rvAssistants.visibility = View.VISIBLE
                            val list = state.data
                            assistantAdapter.submitList(list)
                            binding.tvAssistantCount.text = "${list.size} Assistants Available Nearby"
                        }
                        is UiState.Error -> {
                            binding.layoutSkeleton.visibility = View.GONE
                            binding.rvAssistants.visibility = View.VISIBLE
                            binding.tvAssistantCount.text = "Failed to load assistants. Tap to retry."
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}
