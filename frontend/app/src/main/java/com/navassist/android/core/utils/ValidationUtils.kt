package com.navassist.android.core.utils

import java.util.regex.Pattern

object ValidationUtils {
    private val emailPattern = Pattern.compile(Constants.PATTERN_EMAIL)
    private val otpPattern = Pattern.compile(Constants.PATTERN_OTP_6DIGIT)

    fun isValidEmail(email: String?): Boolean {
        if (email.isNull_or_empty()) return false
        return emailPattern.matcher(email!!).matches()
    }

    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNull_or_empty()) return false
        return phone!!.trim().length >= 10
    }

    fun isValidOtp(otp: String?): Boolean {
        if (otp.isNull_or_empty()) return false
        return otpPattern.matcher(otp!!.trim()).matches()
    }

    fun isValidPassword(password: String?): Boolean {
        if (password.isNull_or_empty()) return false
        return password!!.length >= 6
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
