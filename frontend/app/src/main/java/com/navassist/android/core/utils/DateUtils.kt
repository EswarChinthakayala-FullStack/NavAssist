package com.navassist.android.core.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_INSTANT
    private val displayTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()).withZone(ZoneId.systemDefault())
    private val displayDateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()).withZone(ZoneId.systemDefault())

    fun formatIsoToTime(isoString: String?): String {
        if (isoString.isNull_or_empty()) return ""
        return try {
            val instant = Instant.parse(isoString)
            displayTimeFormatter.format(instant)
        } catch (e: Exception) {
            ""
        }
    }

    fun formatIsoToDate(isoString: String?): String {
        if (isoString.isNull_or_empty()) return ""
        return try {
            val instant = Instant.parse(isoString)
            displayDateFormatter.format(instant)
        } catch (e: Exception) {
            ""
        }
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
