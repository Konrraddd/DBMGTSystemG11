package com.example.eventpass2

data class AttendanceRecord(
    val eventName: String = "",
    val eventDate: String = "",
    val eventLocation: String = "",
    val isEnded: Boolean = false,
    val checkedIn: Boolean = false,
    val timestamp: Long = 0
)