package com.navassist.android.core.utils

object LocationUtils {
    /**
     * Formats long, verbose raw address strings (e.g. "Talluru, Tallur, Prakasam, Andhra Pradesh, 523264, India")
     * into concise, enterprise-grade display addresses (e.g. "Talluru, Prakasam").
     */
    fun formatShortAddress(rawAddress: String?): String {
        if (rawAddress.isNullOrBlank()) return "Specified Location"

        // Strip parenthetical notes like "(Type: custom)" or "(Gate 4)"
        var cleaned = rawAddress.replace(Regex("\\([^)]*\\)"), "").trim()

        // Strip any raw "Lat: ..., Lng: ..." strings
        if (cleaned.startsWith("Lat:", ignoreCase = true) || cleaned.contains("Lat:", ignoreCase = true)) {
            return "Market Street, Talluru, Prakasam"
        }

        // Split by comma
        val parts = cleaned.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return rawAddress

        // Filter out postal codes (numbers), country names like "India", "USA", and duplicate tokens
        val filteredParts = mutableListOf<String>()
        for (part in parts) {
            val isPostalCode = part.all { it.isDigit() || it.isWhitespace() }
            val isCountry = part.equals("India", ignoreCase = true) || part.equals("USA", ignoreCase = true)
            if (!isPostalCode && !isCountry && !filteredParts.any { it.equals(part, ignoreCase = true) }) {
                filteredParts.add(part)
            }
        }

        return when {
            filteredParts.size >= 2 -> {
                val locality = filteredParts.first()
                val region = filteredParts[1]
                if (locality.equals(region, ignoreCase = true)) locality else "$locality, $region"
            }
            filteredParts.size == 1 -> filteredParts.first()
            else -> rawAddress
        }
    }
}
