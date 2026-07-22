package com.navassist.android.presentation.booking

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import org.maplibre.android.camera.CameraPosition
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
    private var isDarkThemeMap = true
    private var hasPlayedEntranceAnimation = false

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
        setupEdgeToEdgeInsets()
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

    override fun setupViews() {
        binding.stepperView.setStep(Step.DESTINATION)
        setupSearchAdapter()
        setupClickListeners()
    }

    private fun setupSearchAdapter() {
        searchAdapter = DestinationSearchAdapter { selectedPoint ->
            val pickup = bookingViewModel.pickupLocation.value
            destinationViewModel.selectDestination(pickup, selectedPoint)
            hideSearchMode()
            updateMapMarkers(pickup, selectedPoint)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupClickListeners() {
        binding.destinationSearchView.onQueryChangedListener = { query ->
            if (query.isBlank()) {
                hideSearchMode()
            } else {
                showSearchMode()
            }
            destinationViewModel.onSearchQueryChanged(query)
        }

        binding.destinationSearchView.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !binding.destinationSearchView.searchEditText.text.isNullOrBlank()) {
                showSearchMode()
            }
        }

        // Map Control 1: My Location / Pickup Center
        binding.btnMyLocation.setOnClickListener { view ->
            animatePress(view) {
                val pickup = bookingViewModel.pickupLocation.value
                if (pickup != null) {
                    mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pickup.latitude, pickup.longitude), 15.0))
                }
            }
        }

        // Map Control 2: Map Style / Layers
        binding.btnMapStyle.setOnClickListener { view ->
            animatePress(view) {
                toggleMapStyle()
            }
        }

        // Map Control 3: Recenter Route
        binding.btnRecenterRoute.setOnClickListener { view ->
            animatePress(view) {
                val pickup = bookingViewModel.pickupLocation.value
                val dest = destinationViewModel.selectedDestination.value
                if (pickup != null && dest != null) {
                    fitBoundsToPoints(pickup.latitude, pickup.longitude, dest.latitude, dest.longitude)
                } else if (pickup != null) {
                    mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pickup.latitude, pickup.longitude), 14.0))
                }
            }
        }

        // Confirm Destination CTA
        binding.cardContinueCta.setOnClickListener { view ->
            val pickup = bookingViewModel.pickupLocation.value
            var dest = destinationViewModel.selectedDestination.value

            // Fallback: if user didn't search but selected location on map center
            if (dest == null) {
                val target = mapLibreMap?.cameraPosition?.target
                val lat = target?.latitude ?: 12.9716
                val lng = target?.longitude ?: 77.5946
                dest = LocationPoint(lat, lng, "Kempegowda International Airport, Devanahalli, Bengaluru", "Kempegowda International Airport")
            }

            animatePress(view) {
                bookingViewModel.setDestination(
                    dest.latitude,
                    dest.longitude,
                    dest.address
                )
                findNavController().navigate(R.id.action_destination_to_schedule)
            }
        }
    }

    private fun showSearchMode() {
        binding.cardSearchResults.visibility = View.VISIBLE
        binding.layoutCenterPin.visibility = View.GONE
        binding.panelMapControls.visibility = View.GONE
        binding.layoutBottomSection.visibility = View.GONE
    }

    private fun hideSearchMode() {
        binding.cardSearchResults.visibility = View.GONE
        binding.layoutCenterPin.visibility = View.VISIBLE
        binding.panelMapControls.visibility = View.VISIBLE
        binding.layoutBottomSection.visibility = View.VISIBLE
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
                                if (state.data.isNotEmpty() && !binding.destinationSearchView.searchEditText.text.isNullOrBlank()) {
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

                // Collect Selected Destination & Journey Stats
                launch {
                    destinationViewModel.selectedDestination.collect { dest ->
                        val pickup = bookingViewModel.pickupLocation.value
                        val pickupAddress = pickup?.address ?: "Pickup Location"
                        val destAddress = dest?.address ?: "Market Street, Talluru, Prakasam"
                        val dist = destinationViewModel.calculatedDistance.value
                        val eta = destinationViewModel.calculatedEtaMins.value

                        binding.cardJourneyPreview.setJourney(pickupAddress, destAddress, dist, eta)
                        binding.cardContinueCta.isEnabled = true
                        binding.cardContinueCta.alpha = 1.0f
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
            val pickup = bookingViewModel.pickupLocation.value
            val dest = destinationViewModel.selectedDestination.value
            if (pickup != null) {
                mapLibreMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(pickup.latitude, pickup.longitude))
                        .title("Pickup: ${pickup.address}")
                )
            }
            if (dest != null) {
                mapLibreMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(dest.latitude, dest.longitude))
                        .title("Destination: ${dest.address}")
                )
            }
            if (pickup != null && dest != null) {
                fitBoundsToPoints(pickup.latitude, pickup.longitude, dest.latitude, dest.longitude)
            } else if (pickup != null) {
                mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(pickup.latitude, pickup.longitude), 14.0))
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        this.mapLibreMap = map

        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isDarkThemeMap = nightModeFlags == Configuration.UI_MODE_NIGHT_YES

        applyMapStyle(isDarkThemeMap)

        val pickup = bookingViewModel.pickupLocation.value
        val initialPos = if (pickup != null) LatLng(pickup.latitude, pickup.longitude) else LatLng(12.9716, 77.5946)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 14.0))

        map.addOnCameraIdleListener {
            val target = map.cameraPosition.target
            target?.let {
                val currentPickup = bookingViewModel.pickupLocation.value
                destinationViewModel.reverseGeocodeLocation(it.latitude, it.longitude, currentPickup)
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
