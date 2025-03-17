package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.Database
import org.example.InstallData

class DataRoute(
    private val logger: Logger
) {

    fun Route.postData() {
        post("/install") {
            val installData = call.receive<InstallData>()
            Database.saveInstall(installData)
            logger.info("Получены данные установки: $installData")
            call.respond(HttpStatusCode.OK, "Installation data received")
        }
    }

    fun Route.getInstallsByBundleId() {
        get("/apps/bundle/{bundleId}") {
            val bundleId = call.parameters["bundleId"]
            if (bundleId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing bundleId")
                return@get
            }
            val installs = Database.getInstallsByBundleId(bundleId)
            if (installs.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No installs for $bundleId")
            } else {
                call.respond(installs)
            }
        }
    }

    fun Route.getInstallsByAppName() {
        get("/apps/name/{appName}") {
            val appName = call.parameters["appName"]
            if (appName == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing appName")
                return@get
            }

            val installs = Database.getInstallsByAppName(appName)
            if (installs.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No Installs for $appName")
            } else {
                call.respond(installs)
            }
        }
    }

    fun Route.getAllApps() {
        get("/apps") {
            call.respond(Database.getAllApps())
        }
    }

}