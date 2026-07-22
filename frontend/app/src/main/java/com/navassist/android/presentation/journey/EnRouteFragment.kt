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
import com.navassist.android.databinding.FragmentEnRouteBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

@AndroidEntryPoint
class EnRouteFragment : BaseFragment<FragmentEnRouteBinding>(FragmentEnRouteBinding::inflate), OnMapReadyCallback {

    private val enRouteViewModel: EnRouteViewModel by viewModels()
    private var mapLibreMap: MapLibreMap? = null

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
        enRouteViewModel.startTracking(10293)

        binding.sheetJourneyDetails.setOnClickListener {
            findNavController().navigate(R.id.action_enroute_to_tracking)
        }

        binding.btnSosFloating.setOnClickListener {
            com.navassist.android.presentation.sos.SosBottomSheet.newInstance()
                .show(childFragmentManager, com.navassist.android.presentation.sos.SosBottomSheet.TAG)
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
            val sfLocation = LatLng(37.7749, -122.4194)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(sfLocation, 14.5), 1000)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Live Tracking Data
                launch {
                    enRouteViewModel.trackingDataState.collect { state ->
                        if (state is UiState.Success) {
                            val data = state.data
                            binding.badgeEtaOverlay.setEtaMinutes(data.etaMinutes)
                            binding.sheetJourneyDetails.updateProgress(data.status, data.distanceKm, data.speedKmH)

                            mapLibreMap?.animateCamera(
                                CameraUpdateFactory.newLatLng(LatLng(data.assistantLat, data.assistantLng)),
                                1200
                            )
                        }
                    }
                }

                // Collect Socket State
                launch {
                    enRouteViewModel.socketState.collect { state ->
                        binding.badgeConnectionStatus.setConnected(state is SocketState.Connected)
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
