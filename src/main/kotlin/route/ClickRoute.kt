package org.example.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import org.example.db.DatabaseClickPostgresSQL
import org.example.utils.JWTConfig

class ClickRoute(private val logger: io.ktor.util.logging.Logger) {

    fun Route.getAllClick() {
        authenticate(JWTConfig.NAME) {
            get("/click/all") {

                val clicks = DatabaseClickPostgresSQL.getAllClick()

                if (clicks.isEmpty()) {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "NOT FOUND: List is Empty!"
                    )
                    return@get
                }

                call.respond(
                    status = HttpStatusCode.OK,
                    DatabaseClickPostgresSQL.getAllClick()
                )
            }
        }
    }

    fun Route.getClickByBundleID() {
        authenticate(JWTConfig.NAME) {
            get("/click/bundleId") {
                val params = call.request.queryParameters
                val bundleID = params["bundleId"]

                val clicks = DatabaseClickPostgresSQL.getClickByBundleID(bundleID)

                if (clicks.isEmpty()) {
                    call.respond(
                        status = HttpStatusCode.NotFound,
                        "NOT FOUND: List is Empty: bundle id is `$bundleID`"
                    )
                    return@get
                }

                call.respond(
                    status = HttpStatusCode.OK,
                    DatabaseClickPostgresSQL.getClickByBundleID(bundleID)
                )
            }
        }
    }

    fun Route.deleteAllClicks() {
        authenticate(JWTConfig.NAME) {
            delete("/click/delete") {
                DatabaseClickPostgresSQL.deleteAllClicks()
                call.respond(
                    status = HttpStatusCode.OK,
                    "Все клики удалены!"
                )
            }
        }
    }
}