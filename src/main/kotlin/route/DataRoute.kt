package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.model.InstallData
import org.example.db.DatabasePostgreSQL
import org.example.utils.FILTER
import javax.xml.crypto.Data

class DataRoute(
    private val logger: Logger
) {

    fun Route.postData() {
        post("/install") {
            val installData = call.receive<InstallData>()
            if (!DatabasePostgreSQL.getAllInstalls().contains(installData)) {
                DatabasePostgreSQL.saveInstall(installData)
                logger.info("Получены данные установки: $installData")
                call.respond(HttpStatusCode.OK, "Installation data received")
            } else {
                logger.info("Данные об установке: $installData уже существуют")
            }
        }
    }


    fun Route.getAllApps() {
        get("/apps") {
            val params = call.request.queryParameters

            val filterName = params[FILTER.NAME.query]
            val bundleID = params[FILTER.BUNDLE_ID.query]

            val fromData = params[FILTER.FROM_DATA.query]
            val toData = params[FILTER.TO_DATA.query]

            val fromApiLevel = params[FILTER.FROM_API_LEVEL.query]
            val toApiLevel = params[FILTER.TO_API_LEVEL.query]

            val fromAndroidApiLevel = params[FILTER.FROM_ANDROID_API_LEVEL.query]
            val toAndroidApiLevel = params[FILTER.TO_ANDROID_API_LEVEL.query]

            call.respond(
                HttpStatusCode.OK,
                DatabasePostgreSQL.getAllInstalls(
                    appName = filterName,
                    bundleID = bundleID,
                    fromData = fromData?.toLong(),
                    toData = toData?.toLong(),
                    fromApiLevel = fromApiLevel?.toIntOrNull(),
                    toApiLevel = toApiLevel?.toIntOrNull(),
                    fromAndroidApiLevel = fromAndroidApiLevel?.toIntOrNull(),
                    toAndroidApiLevel = toAndroidApiLevel?.toIntOrNull()
                )
            )
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

