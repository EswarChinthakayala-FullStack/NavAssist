package com.navassist.android.presentation.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.databinding.FragmentSavedLocationsBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.profile.adapter.SavedLocationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedLocationsFragment : BaseFragment<FragmentSavedLocationsBinding>(FragmentSavedLocationsBinding::inflate) {

    private val locationsViewModel: SavedLocationsViewModel by viewModels()
    private val locationAdapter = SavedLocationAdapter { loc ->
        showToast("Selected ${loc.label}: ${loc.address}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.rvSavedLocations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedLocations.adapter = locationAdapter

        binding.fabAddLocation.setOnClickListener {
            locationsViewModel.addLocation("Gym", "45 Fitness Ave, Downtown", "Near Central Park", "⭐")
            showToast("New saved location added!")
        }

        locationsViewModel.loadSavedLocations()
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                locationsViewModel.locationsState.collect { state ->
                    if (state is UiState.Success) {
                        locationAdapter.submitList(state.data)
                    }
                }
            }
        }
    }
}
