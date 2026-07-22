package com.navassist.android.presentation.booking

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import javax.inject.Inject

enum class ScheduleMode {
    RIDE_NOW,
    SCHEDULE_LATER
}

@HiltViewModel
class ScheduleDateTimeViewModel @Inject constructor() : ViewModel() {

    private val _scheduleMode = MutableStateFlow(ScheduleMode.RIDE_NOW)
    val scheduleMode: StateFlow<ScheduleMode> = _scheduleMode.asStateFlow()

    private val _selectedDateMillis = MutableStateFlow<Long?>(null)
    val selectedDateMillis: StateFlow<Long?> = _selectedDateMillis.asStateFlow()

    private val _selectedHour = MutableStateFlow<Int?>(null)
    val selectedHour: StateFlow<Int?> = _selectedHour.asStateFlow()

    private val _selectedMinute = MutableStateFlow<Int?>(null)
    val selectedMinute: StateFlow<Int?> = _selectedMinute.asStateFlow()

    fun setScheduleMode(mode: ScheduleMode) {
        _scheduleMode.value = mode
    }

    fun setSelectedDate(dateMillis: Long) {
        _selectedDateMillis.value = dateMillis
    }

    fun setSelectedTime(hour: Int, minute: Int) {
        _selectedHour.value = hour
        _selectedMinute.value = minute
    }

    fun getFinalScheduledTimestamp(): Long? {
        if (_scheduleMode.value == ScheduleMode.RIDE_NOW) return null

        val dateMillis = _selectedDateMillis.value ?: return null
        val hour = _selectedHour.value ?: return null
        val minute = _selectedMinute.value ?: return null

        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        return cal.timeInMillis
    }
}
