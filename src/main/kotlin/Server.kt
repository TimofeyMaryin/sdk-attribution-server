package org.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.serialization.json.Json
import org.example.route.DataRoute
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

                get("/") {
                    call.respond(HttpStatusCode.OK, "Сервер работает!")
                }

                DataRoute(logger).apply {
                    postData()
                    getInstallsByBundleId()
                    getInstallsByAppName()
                    getAllApps()
                    deleteByInstallID()
                }

            }
        }.start(wait = true)
    }
}