package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.domain.repository.BookingRepository
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.geometry.LatLng
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlin.math.sin

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _bookingState = MutableStateFlow<UiState<Booking>>(UiState.Idle)
    val bookingState: StateFlow<UiState<Booking>> = _bookingState.asStateFlow()

    private val _cancelState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val cancelState: StateFlow<UiState<Unit>> = _cancelState.asStateFlow()

    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    private var routeJob: Job? = null

    fun loadTripDetail(bookingId: String) {
        if (bookingId.isBlank()) {
            _bookingState.value = UiState.Error("Invalid Booking ID")
            return
        }

        _bookingState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.getBookingById(bookingId)
            result.onSuccess { booking ->
                _bookingState.value = UiState.Success(booking)
                fetchOsrmRoute(booking.pickupLocation, booking.destinationLocation)
            }.onFailure { error ->
                _bookingState.value = UiState.Error(error.message ?: "Failed to load trip details")
            }
        }
    }

    private fun fetchOsrmRoute(pickup: LocationPoint, dest: LocationPoint) {
        routeJob?.cancel()
        routeJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val osrmUrl = "https://router.project-osrm.org/route/v1/driving/" +
                        "${pickup.longitude},${pickup.latitude};${dest.longitude},${dest.latitude}" +
                        "?overview=full&steps=true&geometries=geojson"

                val connection = (URL(osrmUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "NavAssistAndroid/1.0 (Mobile; Android)")
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (connection.responseCode == 200) {
                    val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val routes = json.optJSONArray("routes")

                    if (routes != null && routes.length() > 0) {
                        val mainRouteObj = routes.getJSONObject(0)
                        val mainCoords = mainRouteObj.getJSONObject("geometry").getJSONArray("coordinates")
                        val mainPoints = mutableListOf<LatLng>()
                        for (i in 0 until mainCoords.length()) {
                            val coordPair = mainCoords.getJSONArray(i)
                            mainPoints.add(LatLng(coordPair.getDouble(1), coordPair.getDouble(0)))
                        }

                        withContext(Dispatchers.Main) {
                            _routePoints.value = mainPoints
                        }
                        return@launch
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val fallbackPoints = generateInterpolatedRoute(pickup, dest)
            withContext(Dispatchers.Main) {
                _routePoints.value = fallbackPoints
            }
        }
    }

    private fun generateInterpolatedRoute(pickup: LocationPoint, dest: LocationPoint): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val count = 30
        val latSpan = dest.latitude - pickup.latitude
        val lngSpan = dest.longitude - pickup.longitude

        for (i in 0..count) {
            val fraction = i.toDouble() / count
            val curveOffset = sin(fraction * Math.PI) * 0.006
            val lat = pickup.latitude + (latSpan * fraction) + curveOffset
            val lng = pickup.longitude + (lngSpan * fraction)
            points.add(LatLng(lat, lng))
        }
        return points
    }

    fun cancelBooking(bookingId: String) {
        _cancelState.value = UiState.Loading
        viewModelScope.launch {
            val result = bookingRepository.cancelBooking(bookingId)
            result.onSuccess { updatedBooking ->
                _cancelState.value = UiState.Success(Unit)
                _bookingState.value = UiState.Success(updatedBooking)
            }.onFailure { error ->
                _cancelState.value = UiState.Error(error.message ?: "Failed to cancel booking")
            }
        }
    }
}
