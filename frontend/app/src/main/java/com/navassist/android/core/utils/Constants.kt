package com.navassist.android.core.utils

object Constants {
    const val PATTERN_EMAIL = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    const val PATTERN_PHONE_E164 = "^\\+[1-9]\\d{1,14}$"
    const val PATTERN_OTP_6DIGIT = "^\\d{6}$"

    const val EXTRA_BOOKING_ID = "extra_booking_id"
    const val EXTRA_USER_ID = "extra_user_id"

    const val REQUEST_CODE_LOCATION_PERMISSION = 1001
    const val REQUEST_CODE_NOTIFICATION_PERMISSION = 1002
}
