package org.example.model

import kotlinx.serialization.Serializable


@Serializable
data class ClickModel(
    val id: Int? = null,
    val bundleID: String?,
    val impressionClick: Boolean,
    val click: Boolean
)