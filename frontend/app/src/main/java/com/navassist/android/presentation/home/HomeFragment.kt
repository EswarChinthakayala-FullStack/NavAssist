package com.navassist.android.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.navassist.android.R
import com.navassist.android.core.utils.LocationUtils
import com.navassist.android.databinding.FragmentHomeBinding
import com.navassist.android.domain.model.UserRole
import com.navassist.android.presentation.common.state.UiState
import com.navassist.android.presentation.home.adapter.SavedLocationAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var mapLibreMap: MapLibreMap? = null
    private var userLocationMarker: Marker? = null

    private lateinit var savedLocationAdapter: SavedLocationAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isDarkThemeMap = true
    private var hasInitialLocationFix = false
    private var hasPlayedEntranceAnimation = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableLocationComponentAndFetch()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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

        setupEdgeToEdgeInsets()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        playEntranceAnimations()
    }

    private fun setupEdgeToEdgeInsets() {
        // Adjust top header padding according to status bar inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.cardHeader) { headerView, insets ->
            val statusInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val params = headerView.layoutParams as ViewGroup.MarginLayoutParams
            params.topMargin = statusInsets.top + (12 * resources.displayMetrics.density).toInt()
            headerView.layoutParams = params
            insets
        }

        // Adjust bottom elements according to navigation bar inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.cardBookAssistant) { fabView, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = fabView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = navInsets.bottom + (88 * resources.displayMetrics.density).toInt()
            fabView.layoutParams = params
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.rvSavedLocations) { rvView, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = rvView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = navInsets.bottom + (88 * resources.displayMetrics.density).toInt()
            rvView.layoutParams = params
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.pillCurrentLocation) { pillView, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = pillView.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = navInsets.bottom + (140 * resources.displayMetrics.density).toInt()
            pillView.layoutParams = params
            insets
        }
    }

    private fun setupRecyclerView() {
        savedLocationAdapter = SavedLocationAdapter { savedLocation ->
            findNavController().navigate(R.id.action_home_to_bookAssistant)
        }
        binding.rvSavedLocations.adapter = savedLocationAdapter
    }

    private fun setupClickListeners() {
        // Emergency SOS Button with press animation
        binding.fabSos.setOnClickListener { view ->
            animatePress(view) {
                findNavController().navigate(R.id.action_home_to_sos)
            }
        }

        // Notification Bell with press animation
        binding.btnNotifications.setOnClickListener { view ->
            animatePress(view) {
                findNavController().navigate(R.id.notificationsFragment)
            }
        }

        // Search Bar Click with press animation
        binding.searchCard.setOnClickListener { view ->
            view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
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

        binding.ivTargetGps.setOnClickListener { view ->
            animatePress(view) {
                checkLocationPermissionAndFetch()
            }
        }

        // Floating Map Controls with press animation
        binding.btnMyLocation.setOnClickListener { view ->
            animatePress(view) {
                checkLocationPermissionAndFetch()
            }
        }

        binding.btnCompass.setOnClickListener { view ->
            animatePress(view) {
                resetMapCompass()
            }
        }

        binding.btnMapStyle.setOnClickListener { view ->
            animatePress(view) {
                toggleMapStyle()
            }
        }

        binding.btnTraffic.setOnClickListener { view ->
            animatePress(view) {
                Toast.makeText(requireContext(), "Live Safety & Traffic Layer Active", Toast.LENGTH_SHORT).show()
            }
        }

        // Extended FAB: Book Assistant with premium spring animation
        binding.cardBookAssistant.setOnClickListener { view ->
            view.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80)
                .withEndAction {
                    view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(OvershootInterpolator(2.0f))
                        .withEndAction {
                            findNavController().navigate(R.id.action_home_to_bookAssistant)
                        }
                        .start()
                }
                .start()
        }

        // Profile avatar tap -> Navigate to user profile
        binding.layoutAvatar.setOnClickListener { view ->
            animatePress(view) {
                findNavController().navigate(R.id.userProfileFragment)
            }
        }

        // Location pill tap
        binding.pillCurrentLocation.setOnClickListener { view ->
            animatePress(view) {
                checkLocationPermissionAndFetch()
            }
        }
    }

    private fun animatePress(view: View, onComplete: () -> Unit) {
        view.animate()
            .scaleX(0.90f)
            .scaleY(0.90f)
            .setDuration(70)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .setInterpolator(OvershootInterpolator(2.5f))
                    .withEndAction { onComplete() }
                    .start()
            }
            .start()
    }

    private fun playEntranceAnimations() {
        if (hasPlayedEntranceAnimation) return
        hasPlayedEntranceAnimation = true

        val density = resources.displayMetrics.density
        val translateDistance = 30 * density
        val interpolator = DecelerateInterpolator(1.5f)

        val animatedViews = listOf(
            binding.cardHeader to 0L,
            binding.searchCard to 80L,
            binding.panelMapControls to 160L,
            binding.pillCurrentLocation to 200L,
            binding.rvSavedLocations to 240L,
            binding.cardBookAssistant to 280L
        )

        animatedViews.forEach { (view, _) ->
            view.alpha = 0f
            view.translationY = translateDistance
        }

        animatedViews.forEach { (view, delay) ->
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setStartDelay(delay + 200)
                .setInterpolator(interpolator)
                .start()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect User Profile for Greeting Header & Avatar
                launch {
                    viewModel.userProfile.collect { state ->
                        if (state is UiState.Success) {
                            val user = state.data
                            val firstName = user.fullName.split(" ").firstOrNull() ?: user.fullName
                            
                            // Time-based greeting
                            val greeting = getTimeBasedGreeting(firstName)
                            binding.tvGreetingName.text = greeting

                            // Profile Avatar Coil 3 Loading with Initials Fallback
                            if (!user.profilePictureUrl.isNullOrBlank()) {
                                binding.ivProfileAvatar.visibility = View.VISIBLE
                                binding.tvInitials.visibility = View.GONE
                                binding.ivProfileAvatar.load(user.profilePictureUrl)
                            } else {
                                val initials = user.fullName.split(" ")
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .take(2)
                                    .joinToString("")
                                
                                if (initials.isNotEmpty()) {
                                    binding.ivProfileAvatar.visibility = View.GONE
                                    binding.tvInitials.visibility = View.VISIBLE
                                    binding.tvInitials.text = initials
                                } else {
                                    binding.ivProfileAvatar.visibility = View.VISIBLE
                                    binding.tvInitials.visibility = View.GONE
                                }
                            }

                            // Role badge
                            updateRoleBadge(user.role, user.isVerified)
                        }
                    }
                }

                // Collect Saved Locations
                launch {
                    viewModel.savedLocations.collect { locations ->
                        savedLocationAdapter.submitList(locations)
                    }
                }

                // Collect live location name for header sub-text
                launch {
                    viewModel.currentLocationName.collect { locationName ->
                        val formatted = LocationUtils.formatShortAddress(locationName)
                        binding.tvGreetingSub.text = formatted
                        binding.tvCurrentLocationArea.text = formatted
                    }
                }

                // Collect location accuracy for compatibility
                launch {
                    viewModel.currentLocationAccuracy.collect { accuracy ->
                        if (accuracy != null) {
                            val meters = accuracy.toInt()
                            binding.tvLocationAccuracy.text = "±${meters}m"
                        }
                    }
                }
            }
        }
    }

    private fun getTimeBasedGreeting(name: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 5..11 -> "Good Morning, $name 👋"
            hour in 12..16 -> "Good Afternoon, $name 👋"
            hour in 17..21 -> "Good Evening, $name 👋"
            else -> "Good Night, $name 👋"
        }
    }

    private fun updateRoleBadge(role: UserRole, isVerified: Boolean) {
        if (isVerified) {
            binding.tvUserRole.visibility = View.VISIBLE
            binding.tvUserRole.text = when (role) {
                UserRole.ASSISTANT -> getString(R.string.verified_assistant)
                UserRole.GUEST -> getString(R.string.verified_guest)
                UserRole.ADMIN -> getString(R.string.verified_guest)
            }
        } else if (role != UserRole.GUEST) {
            binding.tvUserRole.visibility = View.VISIBLE
            binding.tvUserRole.text = when (role) {
                UserRole.ASSISTANT -> "Assistant"
                else -> getString(R.string.guest_user)
            }
        } else {
            binding.tvUserRole.visibility = View.GONE
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        this.mapLibreMap = map
        
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        isDarkThemeMap = nightMode == Configuration.UI_MODE_NIGHT_YES

        applyMapStyle(isDarkThemeMap)
    }

    private fun applyMapStyle(dark: Boolean) {
        val styleUrl = if (dark) {
            "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
        } else {
            "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
        }

        mapLibreMap?.setStyle(Style.Builder().fromUri(styleUrl)) { _ ->
            enableLocationComponentAndFetch()
        }
    }

    private fun toggleMapStyle() {
        isDarkThemeMap = !isDarkThemeMap
        applyMapStyle(isDarkThemeMap)
        val styleName = if (isDarkThemeMap) "Dark Matter" else "Positron Light"
        Toast.makeText(requireContext(), "Switched to $styleName map", Toast.LENGTH_SHORT).show()
    }

    private fun resetMapCompass() {
        mapLibreMap?.let { map ->
            val resetCamera = CameraPosition.Builder()
                .bearing(0.0)
                .tilt(0.0)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(resetCamera), 800)
        }
    }

    private fun checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableLocationComponentAndFetch()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun enableLocationComponentAndFetch() {
        mapLibreMap?.let { map ->
            map.style?.let { style ->
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        val locationComponent = map.locationComponent
                        locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions.builder(requireContext(), style).build()
                        )
                        locationComponent.isLocationComponentEnabled = true
                        locationComponent.cameraMode = CameraMode.NONE
                        locationComponent.renderMode = RenderMode.COMPASS
                    } catch (e: Exception) {
                        // Ignore if component already active
                    }
                }
            }
        }
        fetchCurrentLocation()
    }

    @Suppress("MissingPermission")
    private fun fetchCurrentLocation() {
        val cancellationToken = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
        ).addOnSuccessListener { location ->
            if (location != null && _binding != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                val accuracy = location.accuracy

                reverseGeocode(location.latitude, location.longitude) { address ->
                    if (_binding != null) {
                        viewModel.updateLocation(latLng, address, accuracy)
                        updateLocationPinOnMap(latLng, address)
                    }
                }

                centerMapOnLocation(latLng)
            } else if (_binding != null) {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null && _binding != null) {
                        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                        reverseGeocode(lastLocation.latitude, lastLocation.longitude) { address ->
                            if (_binding != null) {
                                viewModel.updateLocation(latLng, address, lastLocation.accuracy)
                                updateLocationPinOnMap(latLng, address)
                            }
                        }
                        centerMapOnLocation(latLng)
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateLocationPinOnMap(latLng: LatLng, title: String) {
        mapLibreMap?.let { map ->
            userLocationMarker?.remove()
            userLocationMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Current Location")
                    .snippet(title)
            )
        }
    }

    private fun centerMapOnLocation(latLng: LatLng) {
        mapLibreMap?.let { map ->
            val zoom = if (hasInitialLocationFix) 16.5 else 16.0
            val tilt = if (hasInitialLocationFix) 30.0 else 25.0
            val duration = if (hasInitialLocationFix) 1400 else 1200

            val cameraPosition = CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .tilt(tilt)
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), duration)
            hasInitialLocationFix = true
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double, callback: (String) -> Unit) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val locationParts = mutableListOf<String>()

                val featureName = addr.featureName
                val subLocality = addr.subLocality
                val locality = addr.locality
                val adminArea = addr.adminArea

                if (!subLocality.isNullOrBlank()) {
                    locationParts.add(subLocality)
                } else if (!featureName.isNullOrBlank() && featureName != addr.getAddressLine(0)) {
                    locationParts.add(featureName)
                }

                if (!locality.isNullOrBlank() && locality != subLocality) {
                    locationParts.add(locality)
                } else if (!adminArea.isNullOrBlank()) {
                    locationParts.add(adminArea)
                }

                val result = if (locationParts.isNotEmpty()) {
                    locationParts.joinToString(" • ")
                } else {
                    addr.getAddressLine(0) ?: "Talluru, Prakasam"
                }
                callback(result)
            } else {
                callback("Talluru, Prakasam")
            }
        } catch (e: Exception) {
            callback("Talluru, Prakasam")
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
        userLocationMarker = null
        binding.mapView.onDestroy()
        _binding = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
