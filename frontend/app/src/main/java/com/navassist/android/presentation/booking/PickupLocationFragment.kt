package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentPickupLocationBinding
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.presentation.booking.adapter.LocationSearchAdapter
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

@AndroidEntryPoint
class PickupLocationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentPickupLocationBinding? = null
    private val binding get() = _binding!!

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val pickupViewModel: PickupLocationViewModel by viewModels()

    private var mapLibreMap: MapLibreMap? = null
    private lateinit var searchAdapter: LocationSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        MapLibre.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickupLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.stepperView.setStep(Step.PICKUP)

        setupSearchAdapter()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupSearchAdapter() {
        searchAdapter = LocationSearchAdapter { selectedPoint ->
            pickupViewModel.selectPickupLocation(selectedPoint)
            binding.rvSearchResults.visibility = View.GONE
            moveCameraToLocation(selectedPoint.latitude, selectedPoint.longitude)
        }
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupClickListeners() {
        binding.searchLocationView.onQueryChangedListener = { query ->
            pickupViewModel.onSearchQueryChanged(query)
        }

        binding.cardCurrentLocation.setOnClickListener {
            val userLat = 37.7749
            val userLng = -122.4194
            pickupViewModel.reverseGeocodeLocation(userLat, userLng)
            moveCameraToLocation(userLat, userLng)
        }

        binding.mapControls.onRecenterClickListener = {
            val userLat = 37.7749
            val userLng = -122.4194
            moveCameraToLocation(userLat, userLng)
        }

        binding.cardContinueCta.setOnClickListener { view ->
            val currentPickup = pickupViewModel.selectedPickup.value
            if (currentPickup != null) {
                bookingViewModel.setPickup(
                    currentPickup.latitude,
                    currentPickup.longitude,
                    currentPickup.address
                )
                findNavController().navigate(R.id.action_pickup_to_destination)
            }
        }
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Search Results
                launch {
                    pickupViewModel.searchResults.collect { state ->
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

                // Collect Selected Pickup Location
                launch {
                    pickupViewModel.selectedPickup.collect { point ->
                        if (point != null) {
                            binding.cardCurrentLocation.setAddress(point.address)
                            binding.cardContinueCta.isEnabled = true
                        }
                    }
                }
            }
        }
    }

    private fun moveCameraToLocation(lat: Double, lng: Double) {
        mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15.0))
    }

    override fun onMapReady(map: MapLibreMap) {
        this.mapLibreMap = map
        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
            val initialPos = LatLng(37.7749, -122.4194)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 14.0))

            map.addOnCameraIdleListener {
                val target = map.cameraPosition.target
                target?.let {
                    pickupViewModel.reverseGeocodeLocation(it.latitude, it.longitude)
                }
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
        super.onDestroyView()
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
