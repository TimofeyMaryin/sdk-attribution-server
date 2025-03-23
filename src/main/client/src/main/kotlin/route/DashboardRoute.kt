package org.example.route

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.head
import kotlinx.html.*

class DashboardRoute {

    fun Route.adminRoute() {
        get("/admin") {
            // Получаем токен из Cookie
            val token = call.request.cookies["jwt_token"]

            if (token == null) {
                // Если токен отсутствует, перенаправляем на вход
                call.respondRedirect("/login?error=not_authenticated")
                return@get
            }

            // Делаем запрос к защищённому API
            try {
                val apiResponse = fetchProtectedApi(token)
                val apiText = apiResponse.bodyAsText()


                if (apiResponse.status == HttpStatusCode.OK) {
                    // Если запрос успешен, отображаем данные
                    call.respondHtml {
                        head {
                            title { +"Админ-панель" }
                        }
                        body {
                            h1 { +"Добро пожаловать, ADMIN!" }
                            p { +"Данные API:" }
                            pre { +apiText }
                            a(href = "/logout") { +"Выход" }
                        }
                    }
                } else {
                    // Если запрос вернул ошибку, перенаправляем на повторный вход
                    call.respondRedirect("/login?error=unauthorized")
                }
            } catch (e: Exception) {
                // Обработка ошибок (например, сервер недоступен)
                call.respondRedirect("/login?error=api_unreachable")
            }
        }
    }

}


suspend fun fetchProtectedApi(token: String): HttpResponse {
    val client = HttpClient()
    return client.get("http://192.168.1.227:8080/apps") {
        header("Authorization", "Bearer $token")
    }
}