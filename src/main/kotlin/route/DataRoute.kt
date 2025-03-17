package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.model.InstallData
import org.example.db.DatabasePostgreSQL

class DataRoute(
    private val logger: Logger
) {

    fun Route.postData() {
        post("/install") {
            val installData = call.receive<InstallData>()
            // Database.saveInstall(installData)
            if (!DatabasePostgreSQL.getAllInstalls().contains(installData)) {
                DatabasePostgreSQL.saveInstall(installData)
                logger.info("Получены данные установки: $installData")
                call.respond(HttpStatusCode.OK, "Installation data received")
            } else {
                logger.info("Данные об установке: $installData уже существуют")
            }
        }
    }

    fun Route.getInstallsByBundleId() {
        get("/apps/bundle/{bundleId}") {
            val bundleId = call.parameters["bundleId"]

            if (bundleId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing bundleId")
                return@get
            }

            val installs = DatabasePostgreSQL.getInstallsByBundleID(bundleId)
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

            val installs = DatabasePostgreSQL.getInstallsByAppName(appName)
            if (installs.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No Installs for $appName")
            } else {
                call.respond(installs)
            }
        }
    }

    fun Route.getAllApps() {
        get("/apps") {
            // call.respond(Database.getAllApps())
            call.respond(DatabasePostgreSQL.getAllInstalls())
        }
    }

    fun Route.deleteByInstallID() {
        delete("/apps/delete/{installID}") {
            val appInstallID = call.parameters["installID"]?.toIntOrNull()

            if (appInstallID == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid InstallID")
                return@delete
            }


            try {
                DatabasePostgreSQL.deleteInstallByID(appInstallID)
                call.respond(HttpStatusCode.OK, "Install object with ID $appInstallID has been successfully deleted.")
            } catch (r: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, "Install object with ID $appInstallID not found.")
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "An error occurred while deleting install object. Error: ${e.message}"
                )
            }

        }
    }

}

