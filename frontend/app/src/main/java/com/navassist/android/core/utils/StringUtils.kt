package com.navassist.android.core.utils

import java.util.Locale

object StringUtils {
    fun capitalizeWords(str: String?): String {
        if (str.isNull_or_empty()) return ""
        val nonNullStr = str!!
        return nonNullStr.split(" ").joinToString(" ") { word ->
            word.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    fun maskPhone(phone: String?): String {
        if (phone.isNull_or_empty() || phone!!.length < 6) return phone ?: ""
        val nonNullPhone = phone
        val visibleSuffix = nonNullPhone.takeLast(4)
        return "*******$visibleSuffix"
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
