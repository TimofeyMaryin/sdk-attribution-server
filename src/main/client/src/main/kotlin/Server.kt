package org.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.example.route.DashboardRoute
import org.example.route.LoginRoute
import org.example.route.StartRoute

class Server {

    fun init() {
        embeddedServer(Netty, 8081) {

            install(ContentNegotiation) {
                json(
                    Json { ignoreUnknownKeys = true }
                )
            }


            routing {
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