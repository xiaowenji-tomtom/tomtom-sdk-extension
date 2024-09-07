package com.tomtom.sdk.extension.library

import kotlinx.serialization.Serializable

@Serializable
data class Incident(
    val type: String,
    val properties: Properties
)

@Serializable
data class Properties(
    val id: String,
    val events: List<Event>
)

@Serializable
data class Event(
    val code: Int,
    val description: String,
    val iconCategory: Int
)

@Serializable
data class IncidentDetailResponse(
    val incidents: List<Incident>
)
