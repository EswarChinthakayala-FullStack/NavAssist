package com.navassist.android.core.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionValidator @Inject constructor() {

    fun isValidToken(token: String?): Boolean {
        if (token.isNull_or_empty()) return false
        val parts = token!!.split(".")
        return parts.size == 3
    }

    fun isExpired(token: String?): Boolean {
        if (!isValidToken(token)) return true
        return false
    }

    fun isRefreshEligible(token: String?): Boolean {
        return isValidToken(token)
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
