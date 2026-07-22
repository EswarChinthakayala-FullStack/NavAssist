package com.navassist.android.domain.model

data class EmergencyContact(
    val id: Int,
    val name: String,
    val phone: String,
    val relationship: String = "Guardian",
    val isPrimary: Boolean = false
)
