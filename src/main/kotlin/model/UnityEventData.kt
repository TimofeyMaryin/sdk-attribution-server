package org.example.model

import kotlinx.serialization.Serializable

@Serializable
data class UnityEventData(
    val event: String?,
    val deviceId: String?,
    val campaignId: String?,
    val timestamp: Long?
)
