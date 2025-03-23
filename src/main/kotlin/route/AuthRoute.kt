package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.model.AuthModel
import org.example.model.AuthToken
import org.example.utils.generateToken
import kotlin.math.log

class AuthRoute(
    private val logger: Logger,
) {

    fun Route.logIn() {
        get("/login") {
            val params = call.request.queryParameters

            val name = params["name"]
            val pass = params["pass"]


            if (name == "ADMIN" && pass == "PASS") {
                val token = generateToken("admin")
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "token" to token,
                        "message" to "Вы успешно вошли в систему!"
                    )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Неверно указали имя пользователя!")
            }
        }
    }

    fun Route.logInPost() {
        post("/login") {
            val data = call.receive<AuthModel>()

            if (data.name == "ADMIN" && data.password == "ADMIN") {
                val token = AuthToken(token = generateToken("admin"), message = "Вы успешно вошли в систему!")
                call.respond(HttpStatusCode.OK, token)
                return@post
            }

            val incorrectToken = AuthToken(null, "Неверно указали имя пользователя!")
            call.respond(HttpStatusCode.Unauthorized, incorrectToken)
        }
    }

    fun Route.auth() {
        get("/auth") {
            call.respond(HttpStatusCode.Forbidden, "Здесь будет возможность создать аккаунт!")
        }
    }

}