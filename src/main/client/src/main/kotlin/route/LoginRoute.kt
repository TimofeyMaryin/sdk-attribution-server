package org.example.route

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import kotlinx.html.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.example.model.UserCredentials

class LoginRoute {

    fun Route.logIn() {
        get("/login") {
            call.respondHtml {
                head {
                    title { +"Войти" }
                    style {
                        unsafe {
                            +"""
                            body {
                                display: flex;
                                justify-content: center;
                                align-items: center;
                                height: 100vh;
                                margin: 0;
                                font-family: Arial, sans-serif;
                                background-color: #f4f4f9;
                            }
                            .login-container {
                                background: #ffffff;
                                padding: 2rem;
                                border-radius: 8px;
                                box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
                                width: 100%;
                                max-width: 400px;
                            }
                            .login-container h1 {
                                margin-bottom: 1.5rem;
                                text-align: center;
                                color: #333;
                            }
                            .login-container label {
                                display: block;
                                margin-bottom: 0.5rem;
                                font-weight: bold;
                                color: #333;
                            }
                            .login-container input {
                                width: 100%;
                                padding: 0.5rem;
                                margin-bottom: 1rem;
                                border: 1px solid #ccc;
                                border-radius: 4px;
                            }
                            .login-container button {
                                width: 100%;
                                padding: 0.75rem;
                                border: none;
                                border-radius: 4px;
                                background-color: #007BFF;
                                color: #ffffff;
                                font-size: 1rem;
                                cursor: pointer;
                                transition: background-color 0.3s ease;
                            }
                            .login-container button:hover {
                                background-color: #0056b3;
                            }
                            """
                        }
                    }
                }
                body {
                    div("login-container") {
                        h1 { +"Вход" }
                        form(action = "/submit-login", method = FormMethod.post) {
                            div {
                                label {
                                    htmlFor = "username"
                                    +"Имя пользователя:"
                                }
                                input(type = InputType.text, name = "username") {
                                    required = true
                                }
                            }
                            div {
                                label {
                                    htmlFor = "password"
                                    +"Пароль:"
                                }
                                input(type = InputType.password, name = "password") {
                                    required = true
                                }
                            }
                            button(type = ButtonType.submit) {
                                +"Войти"
                            }
                        }
                    }
                }
            }
        }

    }

    fun Route.submitLogin() {
        post("/submit-login") {
            // Получаем данные из формы
            val params = call.receiveParameters()
            val username = params["username"]
            val password = params["password"]

            if (username != null && password != null) {
                // Выполняем запрос на вход через loginRequest
                val token = loginRequest(username, password)

                if (token != null) {
                    // Сохраняем токен в Cookie
                    call.response.cookies.append("jwt_token", token)

                    // Перенаправляем на защищённую страницу
                    call.respondRedirect("/admin")
                    return@post
                }
            }

            // Если вход не удался
            call.respondRedirect("/login?error=true")
        }
    }

}


suspend fun loginRequest(username: String, password: String): String? {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    val response: HttpResponse = client.post("http://192.168.1.227:8080/login") {
        contentType(ContentType.Application.Json)
        setBody(UserCredentials(username, password))
    }

    return if (response.status == HttpStatusCode.OK) {
        val responseBody = response.bodyAsText()
        Json.parseToJsonElement(responseBody).jsonObject["token"]?.jsonPrimitive?.content
    } else {
        null
    }
}