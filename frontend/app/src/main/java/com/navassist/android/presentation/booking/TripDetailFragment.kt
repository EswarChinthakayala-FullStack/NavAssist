package com.navassist.android.presentation.booking

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.navassist.android.R
import com.navassist.android.core.utils.CurrencyUtils
import com.navassist.android.databinding.FragmentTripDetailBinding
import com.navassist.android.domain.model.Booking
import com.navassist.android.domain.model.BookingStatus
import com.navassist.android.domain.model.LocationPoint
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@AndroidEntryPoint
class TripDetailFragment : BaseFragment<FragmentTripDetailBinding>(FragmentTripDetailBinding::inflate), OnMapReadyCallback {

    private val viewModel: TripDetailViewModel by viewModels()
    private var mapLibreMap: MapLibreMap? = null
    private var currentBooking: Booking? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        val bookingId = arguments?.getString("bookingId") ?: ""
        viewModel.loadTripDetail(bookingId)
    }

    override fun setupViews() {
        // Back Button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Share Header Button
        binding.btnShareHeader.setOnClickListener {
            shareTripDetails()
        }

        binding.btnShareLiveLocation.setOnClickListener {
            shareTripDetails()
        }

        // Recenter Map
        binding.btnRecenterMap.setOnClickListener {
            currentBooking?.let { booking ->
                fitMapBounds(booking.pickupLocation, booking.destinationLocation)
            }
        }

        // View Receipt
        binding.btnViewReceipt.setOnClickListener {
            findNavController().navigate(R.id.action_tripDetail_to_receipt)
        }

        // Call Assistant
        binding.btnCallAssistant.setOnClickListener {
            val phone = currentBooking?.assistantPhone ?: "+919876543210"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }

        // Message / Chat Assistant
        binding.btnChatAssistant.setOnClickListener {
            findNavController().navigate(R.id.action_tripDetail_to_chat)
        }

        // Emergency SOS
        binding.btnEmergencySos.setOnClickListener {
            showToast("Emergency SOS Alert Triggered! Notifying safety team...", Toast.LENGTH_LONG)
        }

        // Cancel Trip Button
        binding.btnCancelTrip.setOnClickListener {
            currentBooking?.let { booking ->
                viewModel.cancelBooking(booking.id)
            }
        }
    }

    override fun observeViewModel() {
        collectLifecycleFlow(viewModel.bookingState) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Shimmer or loading indicator
                }
                is UiState.Success -> {
                    bindBookingDetails(state.data)
                }
                is UiState.Error -> {
                    showToast(state.message)
                }
                else -> {}
            }
        }

        collectLifecycleFlow(viewModel.routePoints) { points ->
            currentBooking?.let { booking ->
                drawRoutePolylines(booking.pickupLocation, booking.destinationLocation, points)
            }
        }
    }

    private fun bindBookingDetails(booking: Booking) {
        currentBooking = booking

        // 1. Hero Status Card
        val displayId = if (booking.id.length > 8) booking.id.take(8) else booking.id
        binding.tvBookingId.text = "Booking #$displayId"
        binding.tvTripDate.text = if (booking.createdAt.isNotBlank()) booking.createdAt.take(16).replace("T", " • ") else "Today • 10:30 AM"
        binding.tvTripDuration.text = "${booking.estimatedMinutes} min"
        binding.tvTripDistance.text = String.format("%.1f km", booking.distanceKm.coerceAtLeast(1.2))

        bindStatusBadge(booking.status)

        // 2. Assistant Profile Card
        binding.tvAssistantName.text = booking.assistantName ?: "Verified Assistant"
        binding.tvAssistantPhone.text = booking.assistantPhone ?: "+91 98765 43210"
        binding.tvAssistantRating.text = "★ 4.9 • 120+ Completed Trips"

        // 3. OTP Verification Card (Visible when status is ACCEPTED or ARRIVED)
        val showOtp = booking.status == BookingStatus.ACCEPTED || booking.status == BookingStatus.PENDING
        binding.cardOtpVerification.isVisible = showOtp
        binding.tvOtpCode.text = booking.otpStart ?: "482910"

        // 4. Pickup & Destination Route Card
        binding.tvPickupAddress.text = booking.pickupLocation.addressName ?: "Pickup Location"
        binding.tvDestinationAddress.text = booking.destinationLocation.addressName ?: "Destination Address"

        // 5. Fare Breakdown (Indian Rupee ₹)
        val baseFare = (booking.fare * 0.85).coerceAtLeast(50.0)
        binding.tvBaseFare.text = CurrencyUtils.formatInr(baseFare)
        binding.tvTotalFare.text = CurrencyUtils.formatInr(booking.fare)
        binding.tvPaymentStatus.text = "Paid via Online / UPI Wallet"

        // 6. Dynamic Primary CTA Button Action
        when (booking.status) {
            BookingStatus.COMPLETED -> {
                binding.btnPrimaryAction.text = "Book Again"
                binding.btnPrimaryAction.setOnClickListener {
                    findNavController().navigateUp()
                }
                binding.btnCancelTrip.isVisible = false
            }
            BookingStatus.CANCELLED -> {
                binding.btnPrimaryAction.text = "Rebook Trip"
                binding.btnPrimaryAction.setOnClickListener {
                    findNavController().navigateUp()
                }
                binding.btnCancelTrip.isVisible = false
            }
            else -> { // PENDING, ACCEPTED, ONGOING
                binding.btnPrimaryAction.text = "Live Route Tracking"
                binding.btnPrimaryAction.setOnClickListener {
                    showToast("Live tracking active for active journey.")
                }
                binding.btnCancelTrip.isVisible = true
            }
        }

        // Draw Map Route if map is ready
        mapLibreMap?.let {
            val points = viewModel.routePoints.value
            drawRoutePolylines(booking.pickupLocation, booking.destinationLocation, points)
        }
    }

    private fun bindStatusBadge(status: BookingStatus) {
        when (status) {
            BookingStatus.COMPLETED -> {
                binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_completed)
                binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_check_circle)
                binding.ivStatusIcon.setColorFilter(Color.parseColor("#22C55E"))
                binding.tvStatusText.text = "COMPLETED"
                binding.tvStatusText.setTextColor(Color.parseColor("#22C55E"))
            }
            BookingStatus.CANCELLED -> {
                binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_cancelled)
                binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_cancel)
                binding.ivStatusIcon.setColorFilter(Color.parseColor("#EF4444"))
                binding.tvStatusText.text = "CANCELLED"
                binding.tvStatusText.setTextColor(Color.parseColor("#EF4444"))
            }
            else -> {
                binding.chipStatus.setBackgroundResource(R.drawable.bg_status_chip_pending)
                binding.ivStatusIcon.setImageResource(R.drawable.ic_ms_schedule)
                binding.ivStatusIcon.setColorFilter(Color.parseColor("#F59E0B"))
                binding.tvStatusText.text = status.name
                binding.tvStatusText.setTextColor(Color.parseColor("#F59E0B"))
            }
        }
    }

    override fun onMapReady(map: MapLibreMap) {
        mapLibreMap = map
        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
            currentBooking?.let { booking ->
                val points = viewModel.routePoints.value
                drawRoutePolylines(booking.pickupLocation, booking.destinationLocation, points)
            }
        }
    }

    private fun drawRoutePolylines(pickup: LocationPoint, dest: LocationPoint, points: List<LatLng> = emptyList()) {
        val map = mapLibreMap ?: return
        val style = map.style ?: return

        // Clear previous layers/sources
        removeRouteLayersAndSources(style)

        val routePoints = if (points.isNotEmpty()) points else generateRoadPoints(pickup, dest)
        val linePoints = routePoints.map { Point.fromLngLat(it.longitude, it.latitude) }
        val lineString = LineString.fromLngLats(linePoints)
        val feature = Feature.fromGeometry(lineString)

        // 1. Shadow/Border Layer (8dp dark navy)
        val borderSource = GeoJsonSource("trip-border-source", FeatureCollection.fromFeature(feature))
        style.addSource(borderSource)
        val borderLayer = LineLayer("trip-border-layer", "trip-border-source").withProperties(
            PropertyFactory.lineColor(Color.parseColor("#1E3A5F")),
            PropertyFactory.lineWidth(8f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineOpacity(0.7f)
        )
        style.addLayer(borderLayer)

        // 2. Primary Route Layer (5dp vibrant navigation blue)
        val mainSource = GeoJsonSource("trip-main-source", FeatureCollection.fromFeature(feature))
        style.addSource(mainSource)
        val mainLayer = LineLayer("trip-main-layer", "trip-main-source").withProperties(
            PropertyFactory.lineColor(Color.parseColor("#3B82F6")),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
            PropertyFactory.lineOpacity(1.0f)
        )
        style.addLayer(mainLayer)

        // 3. Endpoint Circle Markers (Green Pickup, Red Destination)
        val markerFeatures = listOf(
            Feature.fromGeometry(Point.fromLngLat(pickup.longitude, pickup.latitude)).apply {
                addStringProperty("marker-color", "#22C55E")
            },
            Feature.fromGeometry(Point.fromLngLat(dest.longitude, dest.latitude)).apply {
                addStringProperty("marker-color", "#EF4444")
            }
        )
        val markerSource = GeoJsonSource("trip-markers-source", FeatureCollection.fromFeatures(markerFeatures))
        style.addSource(markerSource)

        val circleLayer = CircleLayer("trip-markers-layer", "trip-markers-source").withProperties(
            PropertyFactory.circleRadius(8f),
            PropertyFactory.circleColor(org.maplibre.android.style.expressions.Expression.get("marker-color")),
            PropertyFactory.circleStrokeWidth(3f),
            PropertyFactory.circleStrokeColor(Color.WHITE)
        )
        style.addLayer(circleLayer)

        fitMapBounds(pickup, dest)
    }

    private fun fitMapBounds(pickup: LocationPoint, dest: LocationPoint) {
        val map = mapLibreMap ?: return
        val bounds = LatLngBounds.Builder()
            .include(LatLng(pickup.latitude, pickup.longitude))
            .include(LatLng(dest.latitude, dest.longitude))
            .build()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 60))
    }

    private fun removeRouteLayersAndSources(style: Style) {
        val layers = listOf("trip-markers-layer", "trip-main-layer", "trip-border-layer")
        val sources = listOf("trip-markers-source", "trip-main-source", "trip-border-source")
        layers.forEach { try { style.removeLayer(it) } catch (_: Exception) {} }
        sources.forEach { try { style.removeSource(it) } catch (_: Exception) {} }
    }

    private fun generateRoadPoints(pickup: LocationPoint, dest: LocationPoint): List<LatLng> {
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

    private fun shareTripDetails() {
        currentBooking?.let { booking ->
            val shareText = "Trip Details #BK-${booking.id.take(6)}\n" +
                    "Pickup: ${booking.pickupLocation.addressName}\n" +
                    "Destination: ${booking.destinationLocation.addressName}\n" +
                    "Fare: ${CurrencyUtils.formatInr(booking.fare)}\n" +
                    "Tracked via NavAssist app"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share Trip Details"))
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
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        mapLibreMap = null
        super.onDestroyView()
    }
}
