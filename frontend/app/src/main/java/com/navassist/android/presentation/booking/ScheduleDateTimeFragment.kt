package com.navassist.android.presentation.booking

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.android.material.transition.MaterialSharedAxis
import com.navassist.android.R
import com.navassist.android.databinding.FragmentScheduleDatetimeBinding
import com.navassist.android.presentation.common.base.BaseFragment
import com.navassist.android.presentation.customviews.Step
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ScheduleDateTimeFragment : BaseFragment<FragmentScheduleDatetimeBinding>(FragmentScheduleDatetimeBinding::inflate) {

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private val scheduleViewModel: ScheduleDateTimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun setupViews() {
        binding.stepperView.setStep(Step.SCHEDULE)

        binding.cardRideNow.setOption(
            "Ride Now ⚡",
            "Find an assistant immediately",
            R.drawable.ic_benefit_pickup
        )

        binding.cardScheduleRide.setOption(
            "Schedule Ride 📅",
            "Choose a future date & time",
            R.drawable.ic_feature_tracking
        )

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardRideNow.setOnClickListener {
            scheduleViewModel.setScheduleMode(ScheduleMode.RIDE_NOW)
        }

        binding.cardScheduleRide.setOnClickListener {
            scheduleViewModel.setScheduleMode(ScheduleMode.SCHEDULE_LATER)
        }

        binding.cardCalendarSelector.setOnClickListener {
            showDatePicker()
        }

        binding.cardTimeSelector.setOnClickListener {
            showTimePicker()
        }

        binding.cardContinueCta.setOnClickListener { view ->
            val timestamp = scheduleViewModel.getFinalScheduledTimestamp()
            bookingViewModel.setScheduledAt(timestamp)
            findNavController().navigate(R.id.action_schedule_to_assistants)
        }
    }

    override fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Schedule Mode
                launch {
                    scheduleViewModel.scheduleMode.collect { mode ->
                        val isScheduleLater = mode == ScheduleMode.SCHEDULE_LATER
                        binding.cardRideNow.setSelectedState(!isScheduleLater)
                        binding.cardScheduleRide.setSelectedState(isScheduleLater)

                        binding.layoutDateTimePickers.visibility = if (isScheduleLater) View.VISIBLE else View.GONE
                        updateSummaryCard()
                    }
                }

                // Collect Selected Date
                launch {
                    scheduleViewModel.selectedDateMillis.collect { millis ->
                        if (millis != null) {
                            val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
                            binding.cardCalendarSelector.setDateText(sdf.format(Date(millis)))
                        } else {
                            binding.cardCalendarSelector.setDateText("Select Travel Date")
                        }
                        updateSummaryCard()
                    }
                }

                // Collect Selected Time
                launch {
                    scheduleViewModel.selectedHour.collect { _ ->
                        val hour = scheduleViewModel.selectedHour.value
                        val minute = scheduleViewModel.selectedMinute.value
                        if (hour != null && minute != null) {
                            val amPm = if (hour >= 12) "PM" else "AM"
                            val displayHour = if (hour % 12 == 0) 12 else hour % 12
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
                            binding.cardTimeSelector.setTimeText(formattedTime)
                        } else {
                            binding.cardTimeSelector.setTimeText("Select Travel Time")
                        }
                        updateSummaryCard()
                    }
                }
            }
        }
    }

    private fun updateSummaryCard() {
        val pickup = bookingViewModel.pickupLocation.value?.address ?: "Selected Pickup"
        val destination = bookingViewModel.destinationLocation.value?.address ?: "Selected Destination"
        val mode = scheduleViewModel.scheduleMode.value

        val scheduleSummary = if (mode == ScheduleMode.RIDE_NOW) {
            "Ride Now (Instant Dispatch)"
        } else {
            val timestamp = scheduleViewModel.getFinalScheduledTimestamp()
            if (timestamp != null) {
                val sdf = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
                "Scheduled for ${sdf.format(Date(timestamp))}"
            } else {
                "Schedule Later (Select Date & Time)"
            }
        }

        binding.cardSummaryMini.setSummary(pickup, destination, scheduleSummary)
        binding.cardContinueCta.isEnabled = mode == ScheduleMode.RIDE_NOW || scheduleViewModel.getFinalScheduledTimestamp() != null
    }

    private fun showDatePicker() {
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Travel Date")
            .setCalendarConstraints(constraints)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            scheduleViewModel.setSelectedDate(selection)
        }

        datePicker.show(childFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select Travel Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            scheduleViewModel.setSelectedTime(timePicker.hour, timePicker.minute)
        }

        timePicker.show(childFragmentManager, "TIME_PICKER")
    }
}
