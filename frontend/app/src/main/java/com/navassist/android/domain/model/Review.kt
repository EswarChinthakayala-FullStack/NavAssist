package com.navassist.android.domain.model

data class Review(
    val id: String,
    val reviewerName: String,
    val rating: Float,
    val comment: String,
    val date: String,
    val isVerified: Boolean = true
)
