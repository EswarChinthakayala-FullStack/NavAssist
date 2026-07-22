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
import com.navassist.android.core.websocket.SocketState
import com.navassist.android.databinding.FragmentLiveTrackingBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

@AndroidEntryPoint
class LiveTrackingFragment : BaseFragment<FragmentLiveTrackingBinding>(FragmentLiveTrackingBinding::inflate), OnMapReadyCallback {

    private val trackingViewModel: LiveTrackingViewModel by viewModels()
    private var mapLibreMap: MapLibreMap? = null

    private var currentAssistantPos: LatLng? = null
    private var currentPassengerPos: LatLng? = null

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
        trackingViewModel.startLiveTracking(10293)

        binding.sheetLiveTracking.setOnClickListener {
            findNavController().navigate(R.id.action_tracking_to_picked_up)
        }

        binding.btnSosFloating.setOnClickListener {
            com.navassist.android.presentation.sos.SosBottomSheet.newInstance()
                .show(childFragmentManager, com.navassist.android.presentation.sos.SosBottomSheet.TAG)
        }

        binding.viewMapControls.onRecenterClick = {
            recenterMapBounds()
        }
        binding.viewMapControls.onZoomInClick = {
            mapLibreMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.viewMapControls.onZoomOutClick = {
            mapLibreMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isDark = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val styleUrl = if (isDark) {
            "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"
        } else {
            "https://basemaps.cartocdn.com/gl/positron-gl-style/style.json"
        }
        map.setStyle(Style.Builder().fromUri(styleUrl)) {
            recenterMapBounds()
        }
    }

    private fun recenterMapBounds() {
        val aPos = currentAssistantPos ?: LatLng(37.7749, -122.4194)
        val pPos = currentPassengerPos ?: LatLng(37.7739, -122.4180)

        val bounds = LatLngBounds.Builder()
            .include(aPos)
            .include(pPos)
            .build()

        mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120), 1000)
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Live Tracking Data
                launch {
                    trackingViewModel.trackingState.collect { state ->
                        if (state is UiState.Success) {
                            val data = state.data
                            currentAssistantPos = LatLng(data.assistantLat, data.assistantLng)
                            currentPassengerPos = LatLng(data.passengerLat, data.passengerLng)

                            binding.cardLiveEta.updateMetrics(data.etaMinutes, data.distanceKm, data.speedKmH)
                        }
                    }
                }

                // Collect Connection State
                launch {
                    trackingViewModel.socketState.collect { state ->
                        binding.chipTrackingStatus.setStatus(state is SocketState.Connected)
                    }
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
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}
