package org.example.model

import kotlinx.serialization.Serializable


@Serializable
data class EventData(
    val event: String,
    val bundleId: String,
    val deviceId: String,
    val id: Int? = null,
)
