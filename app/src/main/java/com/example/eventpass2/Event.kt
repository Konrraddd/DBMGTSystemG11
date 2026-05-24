package com.example.eventpass2

data class Event(
    val eventId: String = "",
    val name: String = "",
    val date: String = "",
    val location: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val eventCode: String = "",
    val isEnded: Boolean = false
)