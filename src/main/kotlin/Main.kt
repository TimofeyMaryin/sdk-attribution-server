package org.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import org.slf4j.LoggerFactory
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.example.route.DataRoute

fun main() {
    val logger = LoggerFactory.getLogger("Server")

    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }


        install(CallLogging)

        routing {

            get("/") {
                call.respond(HttpStatusCode.OK, "Сервер работает!")
            }

            DataRoute(logger).apply {
                postData()
                getInstallsByBundleId()
                getInstallsByAppName()
                getAllApps()
            }

        }
    }.start(wait = true)
}

