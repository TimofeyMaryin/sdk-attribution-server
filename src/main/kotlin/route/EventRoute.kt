package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.callback.ActionDataCallback
import org.example.db.DatabaseEventPostgreSQL
import org.example.model.EventData
import org.example.utils.ActionDataState
import javax.swing.Action

class EventRoute(
    private val logger: Logger,
) {

    fun Route.sendEvent() {
        post("/event") {
            val event = call.receive<EventData>()

            if (!DatabaseEventPostgreSQL.getAllEvent().contains(event)) {
                DatabaseEventPostgreSQL.saveEvent(event)
                logger.info("Event received: $event")
                call.respond(HttpStatusCode.OK, "Event received")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Event already exists")
            }

        }
    }


    fun Route.getAllEvents() {
        get("/events") {
            call.respond(DatabaseEventPostgreSQL.getAllEvent())
        }
    }

    fun Route.deleteByEventID() {
        delete("/events/delete/{eventID}") {
            val eventID = call.parameters["eventID"]?.toIntOrNull()

            if (eventID == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid EventID")
                return@delete
            }

        }
    }

    fun Route.deleteEvent() {
        delete("/events/delete") {
            val params = call.request.queryParameters
            val deviceId = params["deviceID"]
            val id = params["id"]
            val isRemoveAllEvents = params["all"]

            var state = Pair(ActionDataState.NONE, "")


            DatabaseEventPostgreSQL.deleteEvent(
                deviceID = deviceId,
                eventId = id?.toIntOrNull(),
                isRemoveAll = isRemoveAllEvents?.toBooleanStrictOrNull(),
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
                call.respond(HttpStatusCode.OK, state.second)
                return@delete
            }

            if (state.first == ActionDataState.ERROR) {
                call.respond(HttpStatusCode.Forbidden, state.second)
                return@delete
            }

            call.respond(HttpStatusCode.BadRequest, "Invalid EventID or DeviceID")

        }
    }

}