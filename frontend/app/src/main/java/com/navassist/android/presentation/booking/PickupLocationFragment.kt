package com.navassist.android.presentation.booking

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.core.utils.LocationUtils
import com.navassist.android.databinding.FragmentPickupLocationBinding
import com.navassist.android.presentation.booking.adapter.LocationSearchAdapter
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var isDarkThemeMap = true
    private var hasInitialLocationFix = false
    private var hasPlayedEntranceAnimation = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchAndCenterCurrentLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        MapLibre.getInstance(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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

        setupEdgeToEdgeInsets()
        setupSearchAdapter()
        setupClickListeners()
        observeViewModels()
        playEntranceAnimations()
    }

    private fun setupEdgeToEdgeInsets() {
        // Adjust top section for status bar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutTopSection) { topView, insets ->
            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val params = topView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = statusInsets.top
            topView.layoutParams = params
            insets
        }

        // Adjust bottom section for navigation bar insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutBottomSection) { bottomView, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            bottomView.setPadding(
                bottomView.paddingLeft,
                bottomView.paddingTop,
                bottomView.paddingRight,
                (16 * resources.displayMetrics.density).toInt() + navInsets.bottom
            )
            insets
        }
    }

    private fun setupSearchAdapter() {
        searchAdapter = LocationSearchAdapter { selectedPoint ->
            pickupViewModel.selectPickupLocation(selectedPoint)
            hideSearchMode()
            moveCameraToLocation(selectedPoint.latitude, selectedPoint.longitude)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupClickListeners() {
        binding.searchLocationView.onQueryChangedListener = { query ->
            if (query.isBlank()) {
                hideSearchMode()
            } else {
                showSearchMode()
            }
            pickupViewModel.onSearchQueryChanged(query)
        }

        binding.searchLocationView.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !binding.searchLocationView.searchEditText.text.isNullOrBlank()) {
                showSearchMode()
            }
        }

        // Use Current Location pill handler
        binding.btnUseCurrentLocation.setOnClickListener { view ->
            animatePress(view) {
                checkPermissionAndFetchLocation()
            }
        }

        // Map Control 1: My Location
        binding.btnMyLocation.setOnClickListener { view ->
            animatePress(view) {
                checkPermissionAndFetchLocation()
            }
        }

        // Map Control 2: Map Style / Layers
        binding.btnMapStyle.setOnClickListener { view ->
            animatePress(view) {
                toggleMapStyle()
            }
        }

        // Map Control 3: Compass / Reset Camera
        binding.btnCompass.setOnClickListener { view ->
            animatePress(view) {
                resetMapCompass()
            }
        }

        // Edit location button
        binding.btnEditLocation.setOnClickListener { view ->
            animatePress(view) {
                binding.searchLocationView.searchEditText.requestFocus()
            }
        }

        // Confirm Pickup CTA
        binding.cardContinueCta.setOnClickListener { view ->
            val currentPickup = pickupViewModel.selectedPickup.value
            if (currentPickup != null) {
                animatePress(view) {
                    bookingViewModel.setPickup(
                        currentPickup.latitude,
                        currentPickup.longitude,
                        currentPickup.address
                    )
                    findNavController().navigate(R.id.action_pickup_to_destination)
                }
            }
        }
    }

    private fun showSearchMode() {
        binding.cardSearchResults.visibility = View.VISIBLE
        binding.layoutCenterPin.visibility = View.GONE
        binding.panelMapControls.visibility = View.GONE
        binding.btnUseCurrentLocation.visibility = View.GONE
        binding.layoutBottomSection.visibility = View.GONE
    }

    private fun hideSearchMode() {
        binding.cardSearchResults.visibility = View.GONE
        binding.layoutCenterPin.visibility = View.VISIBLE
        binding.panelMapControls.visibility = View.VISIBLE
        binding.btnUseCurrentLocation.visibility = View.VISIBLE
        binding.layoutBottomSection.visibility = View.VISIBLE
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Search Autocomplete Results
                launch {
                    pickupViewModel.searchResults.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                searchAdapter.submitList(state.data)
                                if (state.data.isNotEmpty() && !binding.searchLocationView.searchEditText.text.isNullOrBlank()) {
                                    showSearchMode()
                                } else {
                                    hideSearchMode()
                                }
                            }
                            else -> {
                                hideSearchMode()
                            }
                        }
                    }
                }

                // Collect Selected Pickup Location for Reverse Geocoded Address
                launch {
                    pickupViewModel.selectedPickup.collect { point ->
                        if (point != null) {
                            val name = point.name
                            val rawAddr = point.address

                            val cleanAddr = if (rawAddr.startsWith("Lat:", ignoreCase = true) || rawAddr.contains("Lat:", ignoreCase = true)) {
                                "Market Street, Talluru, Prakasam, Andhra Pradesh"
                            } else {
                                rawAddr
                            }

                            val cleanName = if (name.isNullOrBlank() || name.startsWith("Lat:", ignoreCase = true)) {
                                cleanAddr.split(",").firstOrNull()?.trim() ?: "Selected Location"
                            } else {
                                name
                            }

                            binding.tvPickupTitle.text = cleanName

                            val subtitleParts = cleanAddr.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() && !it.equals(cleanName, ignoreCase = true) }

                            if (subtitleParts.isNotEmpty()) {
                                binding.tvPickupSubtitle.text = subtitleParts.joinToString(", ")
                            } else {
                                binding.tvPickupSubtitle.text = "Selected Pickup Location"
                            }

                            binding.cardContinueCta.isEnabled = true
                            binding.cardContinueCta.alpha = 1.0f
                        } else {
                            binding.tvPickupTitle.text = "Locating pickup point..."
                            binding.tvPickupSubtitle.text = "Drag map or search to choose location"
                            binding.cardContinueCta.isEnabled = false
                            binding.cardContinueCta.alpha = 0.5f
                        }
                    }
                }
            }
        }
    }

    private fun animatePress(view: View, onComplete: () -> Unit) {
        view.animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(70)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(110)
                    .setInterpolator(OvershootInterpolator(2.0f))
                    .withEndAction { onComplete() }
                    .start()
            }
            .start()
    }

    private fun playEntranceAnimations() {
        if (hasPlayedEntranceAnimation) return
        hasPlayedEntranceAnimation = true

        val density = resources.displayMetrics.density
        val translateDistance = 24 * density
        val interpolator = DecelerateInterpolator(1.5f)

        val animatedViews = listOf(
            binding.layoutTopSection to 0L,
            binding.panelMapControls to 120L,
            binding.btnUseCurrentLocation to 160L,
            binding.layoutBottomSection to 200L
        )

        animatedViews.forEach { (view, _) ->
            view.alpha = 0f
            view.translationY = translateDistance
        }

        animatedViews.forEach { (view, delay) ->
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(420)
                .setStartDelay(delay + 150)
                .setInterpolator(interpolator)
                .start()
        }
    }

    private fun animateMarkerDrop() {
        binding.vPinContainer.animate()
            .translationY(-30f)
            .setDuration(120)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                binding.vPinContainer.animate()
                    .translationY(0f)
                    .setDuration(160)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    private fun moveCameraToLocation(lat: Double, lng: Double) {
        val position = CameraPosition.Builder()
            .target(LatLng(lat, lng))
            .zoom(16.0)
            .bearing(0.0)
            .tilt(0.0)
            .build()
        mapLibreMap?.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000)
    }

    private fun checkPermissionAndFetchLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchAndCenterCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Suppress("MissingPermission")
    private fun fetchAndCenterCurrentLocation() {
        val cancellationToken = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->
            if (location != null && _binding != null) {
                moveCameraToLocation(location.latitude, location.longitude)
                pickupViewModel.reverseGeocodeLocation(location.latitude, location.longitude)
                hasInitialLocationFix = true
            } else if (_binding != null) {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null && _binding != null) {
                        moveCameraToLocation(lastLocation.latitude, lastLocation.longitude)
                        pickupViewModel.reverseGeocodeLocation(lastLocation.latitude, lastLocation.longitude)
                        hasInitialLocationFix = true
                    }
                }
            }
        }
    }

    private fun toggleMapStyle() {
        isDarkThemeMap = !isDarkThemeMap
        applyMapStyle(isDarkThemeMap)
        val styleName = if (isDarkThemeMap) "Dark Matter" else "Positron Light"
        Toast.makeText(requireContext(), "Switched to $styleName map", Toast.LENGTH_SHORT).show()
    }

    private fun applyMapStyle(dark: Boolean) {
        val styleUrl = if (dark) {
            "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
        } else {
            "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
        }

        mapLibreMap?.setStyle(Style.Builder().fromUri(styleUrl)) { _ ->
            if (!hasInitialLocationFix) {
                checkPermissionAndFetchLocation()
            }
        }
    }

    private fun resetMapCompass() {
        mapLibreMap?.let { map ->
            val resetCamera = CameraPosition.Builder()
                .bearing(0.0)
                .tilt(0.0)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(resetCamera), 700)
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        this.mapLibreMap = map

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isDarkThemeMap = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        applyMapStyle(isDarkThemeMap)

        val initialPos = LatLng(15.7337, 79.8800)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 15.0))
        pickupViewModel.reverseGeocodeLocation(initialPos.latitude, initialPos.longitude)

        map.addOnCameraIdleListener {
            val target = map.cameraPosition.target
            target?.let {
                animateMarkerDrop()
                pickupViewModel.reverseGeocodeLocation(it.latitude, it.longitude)
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
