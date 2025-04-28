package org.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.util.logging.Logger
import kotlinx.serialization.json.Json
import org.example.route.DashboardRoute
import org.example.route.LoginRoute
import org.example.route.StartRoute
import org.example.route.StaticRoute
import java.io.File

class Server(
    private val logger: Logger
) {

    fun init() {
        embeddedServer(Netty, 8081) {

            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true }
                )
            }


            routing {

                staticFiles("/static", File("src/main/resources/static")) {

                }

                StaticRoute(logger).apply {
                    start()
                    dashboardApplication()
                }

                StartRoute().apply {
                    start()
                }

                LoginRoute().apply {
                    logIn()
                    submitLogin()
                }


                DashboardRoute().apply {
                    adminRoute()
                }
            }

        }.start(wait = true)
    }

}