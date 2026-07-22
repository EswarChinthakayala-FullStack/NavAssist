package com.navassist.android.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.databinding.FragmentHomeBinding
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.home.adapter.SavedLocationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

@AndroidEntryPoint
class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var mapLibreMap: MapLibreMap? = null

    private lateinit var savedLocationAdapter: SavedLocationAdapter

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableUserLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        savedLocationAdapter = SavedLocationAdapter { savedLocation ->
            // On saved location clicked -> initiate booking
        }
        binding.rvSavedLocations.adapter = savedLocationAdapter
    }

    private fun setupClickListeners() {
        binding.fabSos.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_sos)
        }

        binding.searchBarView.onSearchClickListener = {
            // Search clicked
        }

        binding.searchBarView.onTargetClickListener = {
            checkLocationPermission()
        }

        binding.cardBookAssistant.setOnClickListener { view ->
            view.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .withEndAction {
                            findNavController().navigate(R.id.action_home_to_bookAssistant)
                        }
                        .start()
                }
                .start()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect User Profile for Greeting
                launch {
                    viewModel.userProfile.collect { state ->
                        if (state is UiState.Success) {
                            val user = state.data
                            val firstName = user.fullName.split(" ").firstOrNull() ?: user.fullName
                            binding.tvGreetingName.text = "Good Morning, $firstName 👋"
                        }
                    }
                }

                // Collect Saved Locations
                launch {
                    viewModel.savedLocations.collect { locations ->
                        savedLocationAdapter.submitList(locations)
                        binding.tvSavedLocationsTitle.visibility = if (locations.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        this.mapLibreMap = map
        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
            val initialPosition = LatLng(37.7749, -122.4194)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, 14.0))
            checkLocationPermission()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableUserLocation() {
        mapLibreMap?.let { map ->
            val userLocation = LatLng(37.7749, -122.4194)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.0))
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
