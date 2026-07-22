package com.navassist.android.presentation.booking

import android.graphics.Color
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
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point

@AndroidEntryPoint
class DestinationFragment : BaseFragment<FragmentDestinationBinding>(FragmentDestinationBinding::inflate), OnMapReadyCallback {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val destinationViewModel: DestinationViewModel by viewModels()

    private var mapLibreMap: MapLibreMap? = null
    private lateinit var searchAdapter: DestinationSearchAdapter
    private var isDarkThemeMap = true
    private var hasPlayedEntranceAnimation = false
    private var isProgrammaticSelection = false
    private var isPinEditingEnabled = false

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
            isProgrammaticSelection = true
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
                    isProgrammaticSelection = true
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
                isPinEditingEnabled = false
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
                        val pickupAddress = pickup?.address ?: "Talluru Bus Stand, Talluru, Prakasam"
                        val destAddress = dest?.address ?: "Darsi Center, Darsi, Prakasam"
                        val dist = destinationViewModel.calculatedDistance.value
                        val eta = destinationViewModel.calculatedEtaMins.value

                        binding.cardJourneyPreview.setJourney(pickupAddress, destAddress, dist, eta)
                        binding.cardContinueCta.isEnabled = true
                        binding.cardContinueCta.alpha = 1.0f
                    }
                }

                // Collect OSRM Distance updates (arrives after route API completes)
                launch {
                    destinationViewModel.calculatedDistance.collect { dist ->
                        val pickup = bookingViewModel.pickupLocation.value
                        val dest = destinationViewModel.selectedDestination.value
                        val pickupAddress = pickup?.address ?: "Talluru Bus Stand, Talluru, Prakasam"
                        val destAddress = dest?.address ?: "Darsi Center, Darsi, Prakasam"
                        val eta = destinationViewModel.calculatedEtaMins.value
                        binding.cardJourneyPreview.setJourney(pickupAddress, destAddress, dist, eta)
                    }
                }

                // Collect OSRM ETA updates (arrives after route API completes)
                launch {
                    destinationViewModel.calculatedEtaMins.collect { eta ->
                        val pickup = bookingViewModel.pickupLocation.value
                        val dest = destinationViewModel.selectedDestination.value
                        val pickupAddress = pickup?.address ?: "Talluru Bus Stand, Talluru, Prakasam"
                        val destAddress = dest?.address ?: "Darsi Center, Darsi, Prakasam"
                        val dist = destinationViewModel.calculatedDistance.value
                        binding.cardJourneyPreview.setJourney(pickupAddress, destAddress, dist, eta)
                    }
                }

                // Collect OSRM Route Polylines
                launch {
                    destinationViewModel.primaryRoutePoints.collect { primaryPoints ->
                        val altPoints = destinationViewModel.altRoutePoints.value
                        val pickup = bookingViewModel.pickupLocation.value
                        val dest = destinationViewModel.selectedDestination.value
                        drawRoutePolylines(pickup, dest, primaryPoints, altPoints)
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
            val primaryPoints = destinationViewModel.primaryRoutePoints.value
            val altPoints = destinationViewModel.altRoutePoints.value
            drawRoutePolylines(pickup, dest, primaryPoints, altPoints)

            if (pickup != null) {
                fitBoundsToPoints(pickup.latitude, pickup.longitude, dest.latitude, dest.longitude)
            } else {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dest.latitude, dest.longitude), 14.0))
            }
        }
    }

    private fun drawRoutePolylines(
        pickup: LocationPoint?,
        dest: LocationPoint?,
        primaryPoints: List<LatLng>,
        altPoints: List<LatLng>
    ) {
        mapLibreMap?.let { map ->
            val style = map.style ?: return@let

            // Remove previous route layers and sources
            removeRouteLayersAndSources(style)

            // 1. Draw Alternative Route (if present)
            if (altPoints.isNotEmpty()) {
                val altLinePoints = altPoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                val altLineString = LineString.fromLngLats(altLinePoints)
                val altFeature = Feature.fromGeometry(altLineString)
                val altSource = GeoJsonSource("alt-route-source", FeatureCollection.fromFeature(altFeature))
                style.addSource(altSource)

                val altLayer = LineLayer("alt-route-layer", "alt-route-source").withProperties(
                    PropertyFactory.lineColor(Color.parseColor("#52525B")),
                    PropertyFactory.lineWidth(6f),
                    PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineOpacity(0.6f)
                )
                style.addLayer(altLayer)
            }

            // 2. Draw Primary Route Shadow/Border (wider, darker, behind main line)
            if (primaryPoints.isNotEmpty()) {
                val linePoints = primaryPoints.map { Point.fromLngLat(it.longitude, it.latitude) }
                val lineString = LineString.fromLngLats(linePoints)
                val routeFeature = Feature.fromGeometry(lineString)

                val borderSource = GeoJsonSource("route-border-source", FeatureCollection.fromFeature(routeFeature))
                style.addSource(borderSource)

                val borderLayer = LineLayer("route-border-layer", "route-border-source").withProperties(
                    PropertyFactory.lineColor(Color.parseColor("#1E3A5F")),
                    PropertyFactory.lineWidth(14f),
                    PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineOpacity(0.7f)
                )
                style.addLayer(borderLayer)

                // 3. Draw Primary Route (vibrant blue, round caps)
                val mainSource = GeoJsonSource("route-main-source", FeatureCollection.fromFeature(routeFeature))
                style.addSource(mainSource)

                val mainLayer = LineLayer("route-main-layer", "route-main-source").withProperties(
                    PropertyFactory.lineColor(Color.parseColor("#3B82F6")),
                    PropertyFactory.lineWidth(10f),
                    PropertyFactory.lineCap(org.maplibre.android.style.layers.Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(org.maplibre.android.style.layers.Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineOpacity(1.0f)
                )
                style.addLayer(mainLayer)
            }

            // 4. Draw Pickup and Destination circle markers via GeoJSON
            addEndpointMarkers(style, pickup, dest)
        }
    }

    private fun addEndpointMarkers(style: Style, pickup: LocationPoint?, dest: LocationPoint?) {
        val features = mutableListOf<Feature>()

        if (pickup != null) {
            val pickupFeature = Feature.fromGeometry(Point.fromLngLat(pickup.longitude, pickup.latitude))
            pickupFeature.addStringProperty("marker-color", "#22C55E")
            pickupFeature.addStringProperty("title", "Pickup")
            features.add(pickupFeature)
        }

        if (dest != null) {
            val destFeature = Feature.fromGeometry(Point.fromLngLat(dest.longitude, dest.latitude))
            destFeature.addStringProperty("marker-color", "#EF4444")
            destFeature.addStringProperty("title", "Destination")
            features.add(destFeature)
        }

        if (features.isNotEmpty()) {
            val markerSource = GeoJsonSource("endpoint-markers-source", FeatureCollection.fromFeatures(features))
            style.addSource(markerSource)

            val circleLayer = org.maplibre.android.style.layers.CircleLayer("endpoint-markers-layer", "endpoint-markers-source")
                .withProperties(
                    PropertyFactory.circleRadius(8f),
                    PropertyFactory.circleColor(org.maplibre.android.style.expressions.Expression.get("marker-color")),
                    PropertyFactory.circleStrokeWidth(3f),
                    PropertyFactory.circleStrokeColor(Color.WHITE)
                )
            style.addLayer(circleLayer)
        }
    }

    private fun removeRouteLayersAndSources(style: Style) {
        val layerIds = listOf("endpoint-markers-layer", "route-main-layer", "route-border-layer", "alt-route-layer")
        val sourceIds = listOf("endpoint-markers-source", "route-main-source", "route-border-source", "alt-route-source")

        layerIds.forEach { id ->
            try { style.removeLayer(id) } catch (_: Exception) {}
        }
        sourceIds.forEach { id ->
            try { style.removeSource(id) } catch (_: Exception) {}
        }
    }

    private fun fitBoundsToPoints(lat1: Double, lon1: Double, lat2: Double, lon2: Double) {
        mapLibreMap?.let { map ->
            try {
                val bounds = LatLngBounds.Builder()
                    .include(LatLng(lat1, lon1))
                    .include(LatLng(lat2, lon2))
                    .build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            } catch (e: Exception) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat2, lon2), 14.0))
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
            val pickup = bookingViewModel.pickupLocation.value
            val dest = destinationViewModel.selectedDestination.value
            val primaryPoints = destinationViewModel.primaryRoutePoints.value
            val altPoints = destinationViewModel.altRoutePoints.value
            drawRoutePolylines(pickup, dest, primaryPoints, altPoints)

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
        val initialPos = if (pickup != null) LatLng(pickup.latitude, pickup.longitude) else LatLng(15.7337, 79.8800)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 14.0))

        map.addOnCameraIdleListener {
            if (isProgrammaticSelection || !isPinEditingEnabled) {
                isProgrammaticSelection = false
                return@addOnCameraIdleListener
            }
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
