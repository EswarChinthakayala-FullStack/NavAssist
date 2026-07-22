package com.navassist.android.core.network

import java.util.UUID

object RequestIdGenerator {
    fun generate(): String {
        return UUID.randomUUID().toString()
    }
}
