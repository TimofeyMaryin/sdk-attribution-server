package org.example.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse

suspend fun fetchProtectedApi(token: String): HttpResponse {
    val client = HttpClient()
    return client.get("${URL_TO_SERVER}/apps") {
        header("Authorization", "Bearer $token")
    }
}
