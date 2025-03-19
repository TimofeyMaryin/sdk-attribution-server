package org.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import org.example.model.UnityEventData
import org.example.route.DataRoute
import org.example.route.EventRoute
import org.slf4j.event.Level

class Server(
    private val logger: Logger
) {
    fun init() {
        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true }
                )
            }
            install(CallLogging) { level = Level.INFO }

            routing {

                post("/unity-callback") {
                    val eventData = call.receive<UnityEventData>()
                    println("Полеченно событие от Unity ADS: $eventData")
                    processUnityEvent(eventData)

                    call.respond(HttpStatusCode.OK, eventData)
                }

                get("/") {
                    call.respond(HttpStatusCode.OK, "Сервер работает!")
                }

                DataRoute(logger).apply {
                    postData()
                    getAllApps()
                    deleteByInstallID()
                }

                EventRoute(logger).apply {
                    sendEvent()
                    getAllEvents()
                    deleteByEventID()
                }

            }
        }.start(wait = true)
    }
}


fun processUnityEvent(data: UnityEventData) {
    when (data.event) {
        "install" -> println("Новая установка: ${data.deviceId}, кампания: ${data.campaignId}")
        "click" -> println("Клик: ${data.deviceId}")
        else -> println("Неизвестное событие: ${data.event}")
    }
    // Здесь можешь сохранять данные в базу, логировать или что-то ещё
}