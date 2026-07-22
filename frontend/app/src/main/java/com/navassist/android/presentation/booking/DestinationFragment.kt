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
import com.navassist.android.databinding.FragmentDestinationBinding
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.presentation.booking.adapter.DestinationSearchAdapter
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

@AndroidEntryPoint
class DestinationFragment : BaseFragment<FragmentDestinationBinding>(FragmentDestinationBinding::inflate), OnMapReadyCallback {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val destinationViewModel: DestinationViewModel by viewModels()

    private var mapLibreMap: MapLibreMap? = null
    private lateinit var searchAdapter: DestinationSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        MapLibre.getInstance(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.DESTINATION)
        setupSearchAdapter()
        setupClickListeners()
    }

    private fun setupSearchAdapter() {
        searchAdapter = DestinationSearchAdapter { selectedPoint ->
            val pickup = bookingViewModel.pickupLocation.value
            destinationViewModel.selectDestination(pickup, selectedPoint)
            binding.rvSearchResults.visibility = View.GONE
            updateMapMarkers(pickup, selectedPoint)
        }
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupClickListeners() {
        binding.destinationSearchView.onQueryChangedListener = { query ->
            destinationViewModel.onSearchQueryChanged(query)
        }

        binding.mapControls.onRecenterClickListener = {
            val pickup = bookingViewModel.pickupLocation.value
            val dest = destinationViewModel.selectedDestination.value
            if (pickup != null && dest != null) {
                fitBoundsToPoints(pickup.latitude, pickup.longitude, dest.latitude, dest.longitude)
            } else if (pickup != null) {
                mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pickup.latitude, pickup.longitude), 14.0))
            }
        }

        binding.cardContinueCta.setOnClickListener { view ->
            val currentDest = destinationViewModel.selectedDestination.value
            if (currentDest != null) {
                bookingViewModel.setDestination(
                    currentDest.latitude,
                    currentDest.longitude,
                    currentDest.address
                )
                findNavController().navigate(R.id.action_destination_to_schedule)
            }
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Search Results
                launch {
                    destinationViewModel.searchResults.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                searchAdapter.submitList(state.data)
                                binding.rvSearchResults.visibility = if (state.data.isEmpty()) View.GONE else View.VISIBLE
                            }
                            else -> {
                                binding.rvSearchResults.visibility = View.GONE
                            }
                        }
                    }
                }

                // Collect Selected Destination & Journey Stats
                launch {
                    destinationViewModel.selectedDestination.collect { dest ->
                        val pickup = bookingViewModel.pickupLocation.value
                        val pickupAddress = pickup?.address ?: "Pickup Location"
                        val destAddress = dest?.address ?: "Destination Location"
                        val dist = destinationViewModel.calculatedDistance.value
                        val eta = destinationViewModel.calculatedEtaMins.value

                        binding.cardJourneyPreview.setJourney(pickupAddress, destAddress, dist, eta)
                        binding.cardContinueCta.isEnabled = dest != null
                    }
                }
            }
        }
    }

    private fun updateMapMarkers(pickup: LocationPoint?, dest: LocationPoint) {
        mapLibreMap?.let { map ->
            map.clear()
            if (pickup != null) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(pickup.latitude, pickup.longitude))
                        .title("Pickup: ${pickup.address}")
                )
            }
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(dest.latitude, dest.longitude))
                    .title("Destination: ${dest.address}")
            )

            if (pickup != null) {
                fitBoundsToPoints(pickup.latitude, pickup.longitude, dest.latitude, dest.longitude)
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dest.latitude, dest.longitude), 14.0))
            }
        }
    }

    private fun fitBoundsToPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
        mapLibreMap?.let { map ->
            val bounds = LatLngBounds.Builder()
                .include(LatLng(lat1, lon1))
                .include(LatLng(lat2, lon2))
                .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        this.mapLibreMap = map
        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
            val pickup = bookingViewModel.pickupLocation.value
            if (pickup != null) {
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(pickup.latitude, pickup.longitude))
                        .title("Pickup: ${pickup.address}")
                )
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pickup.latitude, pickup.longitude), 14.0))
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.7749, -122.4194), 14.0))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
