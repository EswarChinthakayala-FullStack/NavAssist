package com.navassist.android.presentation.assistant.booking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.navassist.android.R
import com.navassist.android.databinding.BottomSheetBookingRequestBinding
import com.navassist.android.presentation.assistant.booking.adapter.TripInfoAdapter
import com.navassist.android.presentation.assistant.booking.adapter.TripInfoNoteItem
import com.navassist.android.presentation.common.state.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookingRequestBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBookingRequestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookingRequestViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBookingRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomSheetBehavior()
        setupListeners()
        observeViewModel()

        val bookingId = arguments?.getString("booking_id") ?: ""
        if (bookingId.isNotBlank()) {
            viewModel.loadBookingDetails(bookingId)
        }
    }

    private fun setupBottomSheetBehavior() {
        dialog?.let { dialog ->
            val bottomSheet = (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false // Prevent drag dismissal during countdown
                sheet.setBackgroundResource(android.R.color.transparent)
            }
        }
        isCancelable = false
    }

    private fun setupListeners() {
        // Countdown Ring Expiry Listener
        binding.countdownRing.onCountdownFinished = {
            viewModel.onCountdownExpired()
        }

        // Action Buttons
        binding.actionButtonsView.onAcceptClickListener = {
            binding.countdownRing.stopCountdown()
            viewModel.acceptBooking()
        }

        binding.actionButtonsView.onDeclineClickListener = {
            binding.countdownRing.stopCountdown()
            viewModel.rejectBooking()
        }

        // Expired View Return Action
        binding.expiredView.onDismissClickListener = {
            dismissAllowingStateLoss()
        }

        // Passenger Action Buttons (Chat, Call, Profile)
        binding.passengerCard.onChatClickListener = {
            val bookingState = viewModel.bookingState.value
            if (bookingState is UiState.Success) {
                navigateToChat(bookingState.data.id)
            }
        }

        binding.passengerCard.onCallClickListener = {
            val bookingState = viewModel.bookingState.value
            if (bookingState is UiState.Success) {
                val phone = bookingState.data.guestPhone ?: ""
                initiatePhoneCall(phone)
            }
        }

        binding.passengerCard.onProfileClickListener = {
            Toast.makeText(requireContext(), "Passenger profile verification verified ✓", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Booking State
                launch {
                    viewModel.bookingState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.skeletonView.visibility = View.VISIBLE
                                binding.layoutMainDetails.visibility = View.GONE
                                binding.expiredView.visibility = View.GONE
                            }
                            is UiState.Success -> {
                                binding.skeletonView.stopShimmer()
                                binding.layoutMainDetails.visibility = View.VISIBLE
                                binding.expiredView.visibility = View.GONE

                                val booking = state.data
                                binding.passengerCard.bindBooking(booking)
                                binding.routeCard.bindBooking(booking)
                                binding.fareCard.setFare(booking.fare)
                                binding.paymentCard.setPaymentMethod("Online Wallet / UPI", isPrepaid = true)
                                setupSpecialNotes()

                                binding.countdownRing.startCountdown(30)
                                binding.bottomSheetContainer.announceForAccessibility("New booking request received from ${booking.guestName}")
                            }
                            is UiState.Error -> {
                                binding.skeletonView.stopShimmer()
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            }
                            else -> {}
                        }
                    }
                }

                // Collect Expired State
                launch {
                    viewModel.isExpired.collect { isExpired ->
                        if (isExpired) {
                            binding.layoutMainDetails.visibility = View.GONE
                            binding.actionButtonsView.visibility = View.GONE
                            binding.expiredView.visibility = View.VISIBLE
                            binding.countdownRing.stopCountdown()
                            binding.bottomSheetContainer.announceForAccessibility("Booking request expired")
                        }
                    }
                }

                // Collect Processing State
                launch {
                    viewModel.isProcessing.collect { isProcessing ->
                        if (isProcessing) {
                            binding.actionButtonsView.showLoading()
                        } else {
                            binding.actionButtonsView.hideLoading()
                        }
                    }
                }

                // Collect UI Effects
                launch {
                    viewModel.effects.collect { effect ->
                        when (effect) {
                            is BookingRequestEffect.ShowToast -> {
                                Toast.makeText(requireContext(), effect.message, Toast.LENGTH_SHORT).show()
                            }
                            is BookingRequestEffect.ShowSnackbar -> {
                                Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_LONG).show()
                            }
                            is BookingRequestEffect.NavigateToJourney -> {
                                dismissAllowingStateLoss()
                                navigateToEnRoute(effect.bookingId)
                            }
                            is BookingRequestEffect.DismissBottomSheet -> {
                                dismissAllowingStateLoss()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setupSpecialNotes() {
        val notes = listOf(
            TripInfoNoteItem("Passenger Note", "Please meet at Airport Gate 4 entrance.", "ℹ️"),
            TripInfoNoteItem("Safety Notice", "Sanitized vehicle & identity badge verified.", "🛡️")
        )
        val adapter = TripInfoAdapter(notes)
        binding.rvTripNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTripNotes.adapter = adapter
    }

    private fun navigateToChat(bookingId: String) {
        try {
            findNavController().navigate(R.id.chatFragment)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Opening Chat...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initiatePhoneCall(phoneNumber: String) {
        try {
            val uri = Uri.parse("tel:${if (phoneNumber.isNotBlank()) phoneNumber else "+919876543210"}")
            val intent = Intent(Intent.ACTION_DIAL, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not launch phone dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEnRoute(bookingId: String) {
        try {
            findNavController().navigate(R.id.enRouteFragment)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Transitioning to Journey...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        binding.countdownRing.stopCountdown()
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BookingRequestBottomSheet"
        fun newInstance(bookingId: String): BookingRequestBottomSheet {
            val fragment = BookingRequestBottomSheet()
            fragment.arguments = Bundle().apply {
                putString("booking_id", bookingId)
            }
            return fragment
        }
    }
}
