package com.example.eventpass2

data class Participant(
    val uid: String = "",
    val name: String = "",
    val qr_id: String = "",
    val checked_in: Boolean = false,
    val timestamp: Long = 0
)