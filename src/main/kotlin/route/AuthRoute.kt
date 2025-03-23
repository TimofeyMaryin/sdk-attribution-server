package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.callback.ActionUserCallback
import org.example.model.UserModel
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

    fun Route.auth() {
        get("/auth") {
            call.respond(HttpStatusCode.Forbidden, "Здесь будет возможность создать аккаунт!")
        }
    }

}