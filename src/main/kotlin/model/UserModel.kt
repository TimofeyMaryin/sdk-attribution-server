package org.example.model

import kotlinx.serialization.Serializable


@Serializable
data class UserModel(
    val id: Int? = null,
    val name: String,
    val password: String,
    val token: String?,
)
