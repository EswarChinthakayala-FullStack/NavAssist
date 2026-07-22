package com.navassist.android.core.network

import okhttp3.CertificatePinner

object CertificatePinning {
    fun create(): CertificatePinner {
        return CertificatePinner.Builder()
            // Configured pins for API domains in production/staging environments
            .add("api.navassist.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .add("staging-api.navassist.com", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
            .build()
    }
}
