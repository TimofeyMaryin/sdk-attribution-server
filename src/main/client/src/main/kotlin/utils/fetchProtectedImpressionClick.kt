package org.example.utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse

suspend fun fetchProtectedImpressionClick(token: String, bundleId: String): HttpResponse {
    val client = HttpClient()
    return client.get("${URL_TO_SERVER}/click/bundleId?bundleId=$bundleId") {
        header("Authorization", "Bearer $token")
    }
}
