package org.example.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.model.InstallData

suspend fun findApplicationByName(name: String?): InstallData? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    try {
        println("findApplicationByName: start function")
        val response: HttpResponse = client.get("${URL_TO_SERVER}apps") {
            url {
                parameters.append("name", name!!)
            }
        }
        println("findApplicationByName: response $response")

        return if (response.status == HttpStatusCode.OK) {
            val responseBody = response.bodyAsText()
            val apps = Json.decodeFromString<List<InstallData>>(responseBody)
            apps.firstOrNull() // Возвращаем первое подходящее приложение
        } else {
            null
        }
    } catch (e: NullPointerException) {
        println("findApplicationByName - filed `name` equals NULL! Application not found")
        return null
    }

}
