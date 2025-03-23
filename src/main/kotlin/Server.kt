package org.example

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
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
import org.example.route.AuthRoute
import org.example.route.DataRoute
import org.example.route.EventRoute
import org.example.utils.JWTConfig
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

            install(Authentication) {
                jwt(name = JWTConfig.NAME) {
                    realm = JWTConfig.REALM

                    verifier(
                        JWT
                            .require(Algorithm.HMAC256(JWTConfig.SECRETE_KEY))
                            .withAudience(JWTConfig.AUDIENCE)
                            .withIssuer(JWTConfig.ISSUER)
                            .build()
                    )

                    validate { jwtCredential ->
                        if (jwtCredential.payload.audience.contains(JWTConfig.AUDIENCE)) {
                            JWTPrincipal(jwtCredential.payload)
                        } else {
                            null
                        }
                    }

                    challenge { _, _ ->
                        call.respond(HttpStatusCode.Unauthorized, "Токен недействительный или отсутствует")
                    }
                }
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

                    deleteInstall()
                }

                EventRoute(logger).apply {
                    sendEvent()
                    getAllEvents()
                    deleteEvent()
                }

                AuthRoute(logger).apply {
                    logIn()
                    logInPost()
                    auth()
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