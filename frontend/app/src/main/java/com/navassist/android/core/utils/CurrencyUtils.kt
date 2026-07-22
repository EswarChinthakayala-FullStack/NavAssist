package com.navassist.android.core.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    fun formatInr(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        format.currency = Currency.getInstance("INR")
        return format.format(amount)
    }

    fun formatUsd(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance("USD")
        return format.format(amount)
    }
}
