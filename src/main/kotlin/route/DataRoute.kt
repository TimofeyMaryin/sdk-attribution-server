package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.callback.ActionDataCallback
import org.example.model.InstallData
import org.example.db.DatabasePostgreSQL
import org.example.utils.FILTER
import org.example.utils.ActionDataState

class DataRoute(
    private val logger: Logger
) {

    fun Route.postData() {
        post("/install") {
            val installData = call.receive<InstallData>()
            var status = Pair(ActionDataState.NONE, "")

            logger.info("Install received: $installData")


            DatabasePostgreSQL.saveInstall(
                installData,
                callback = object : ActionDataCallback {
                    override fun onSuccess(msg: String) {
                        logger.info("DatabasePostgreSQL.saveInstall SUCCESS: $msg")
                         status = status.copy(
                             first = ActionDataState.SUCCESS,
                             second = msg
                         )
                    }

                    override fun onError(e: String) {
                        logger.error("DatabasePostgreSQL.saveInstall ERROR: $e")
                        status = status.copy(
                            first = ActionDataState.ERROR,
                            second = e
                        )
                    }

                }
            )

            if (status.first == ActionDataState.SUCCESS) {
                call.respond(HttpStatusCode.OK, status.second)
            }
            if (status.first == ActionDataState.ERROR) {
                call.respond(HttpStatusCode.Forbidden, status.second)
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

            val country = params[FILTER.COUNTRY.query]

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
                    toAndroidApiLevel = toAndroidApiLevel?.toIntOrNull(),
                    country = country,
                )
            )
        }
    }

    fun Route.deleteInstall() {
        delete("/apps/delete") {
            val params = call.request.queryParameters

            val deviceId = params["deviceId"]
            val applicationName = params["appName"]

            var state = Pair(ActionDataState.NONE, "")

            DatabasePostgreSQL.deleteInstall(
                deviceId,
                applicationName,
                callback = object : ActionDataCallback {
                    override fun onSuccess(msg: String) {
                        state = Pair(ActionDataState.SUCCESS, msg)
                    }

                    override fun onError(e: String) {
                        state = Pair(ActionDataState.ERROR, e)
                    }
                }
            )

            if (state.first == ActionDataState.SUCCESS) {
                call.respond(HttpStatusCode.OK, "SUCCESS: ${state.second}")
                return@delete
            }

            if (state.first == ActionDataState.ERROR) {
                call.respond(HttpStatusCode.Forbidden, "ERROR: ${state.second}")
                return@delete
            }

        }
    }


}

