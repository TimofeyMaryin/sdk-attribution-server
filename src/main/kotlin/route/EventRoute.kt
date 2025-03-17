package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import org.example.db.DatabaseEventPostgreSQL
import org.example.model.EventData

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

}