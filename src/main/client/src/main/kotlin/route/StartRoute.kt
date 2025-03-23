package org.example.route

import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kotlinx.html.*

class StartRoute {

    fun Route.start() {
        get("/") {
            call.respondHtml {
                head {
                    title { +"Мой сайт на Ktor" }
                }
                body {
                    h1 { +"Добро пожаловать на мой сайт!" }
                    p { +"Это пример сайта, созданного с использованием Kotlin и Ktor" }
                }
            }
        }
    }

}