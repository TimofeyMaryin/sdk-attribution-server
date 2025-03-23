package org.example.model

import kotlinx.serialization.Serializable


@Serializable
data class AuthModel(
    val name: String,
    val password: String
)

@Serializable
data class AuthToken(
    val token: String?,
    val message: String,
)
