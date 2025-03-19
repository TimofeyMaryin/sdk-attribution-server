package org.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.callback.ActionDataCallback
import org.example.db.DatabasePostgreSQL.Install
import org.example.model.EventData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseEventPostgreSQL {

    val db_event by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/sdk_server_event"
            driverClassName = "org.postgresql.Driver"
            username = "user"
            password = "pass"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        HikariDataSource(config)
    }


    object Event : Table() {
        val id = integer("id").autoIncrement()
        val event = varchar("event", 255)
        val bundleId = varchar("bundleId", 255)
        val deviceId = varchar("deviceId", 255)
        override val primaryKey = PrimaryKey(id)
    }


    fun init() {
        transaction(Database.connect(db_event)) {
            SchemaUtils.create(Event)
        }
    }

    fun saveEvent(event: EventData) = transaction(Database.connect(db_event)) {

        if (!getAllEvent().contains(event)) {
            Event.insert {
                it[Event.event] = event.event
                it[bundleId] = event.bundleId
                it[deviceId] = event.deviceId
            }
        }
    }

   fun getAllEvent() = transaction(Database.connect(db_event)) {
       Event.selectAll().map {
           EventData(
               event = it[Event.event],
               bundleId = it[Event.bundleId],
               deviceId = it[Event.deviceId],
               id = it[Event.id]
           )
       }
   }

    fun deleteEventByDeviceID(deviceID: String?) = transaction(Database.connect(db_event)) {
        if (deviceID == null) {
            Event.deleteAll()
            return@transaction
        }

        val rowsDetect = Event.deleteWhere { Event.deviceId eq deviceID }
        if (rowsDetect == 0) {
            println("No Event with ID $deviceID")
        } else {
            println("Event with ID $deviceID deleted")
        }

    }


    fun deleteEvent(
        deviceID: String? = null,
        eventId: Int? = null,
        isRemoveAll: Boolean? = null,
        callback: ActionDataCallback,
    ) = transaction(Database.connect(db_event)) {

        if (deviceID != null) {
            val rowDetected = Event.deleteWhere { Event.deviceId eq deviceID }
            if (rowDetected == 0 ) {
                callback.onError("Event with deviceID: $deviceID does not found")
                return@transaction
            }
            callback.onSuccess("All Events with deviceId: $deviceID has been remove")
            return@transaction
        }

        if (eventId != null) {
            val rowDetected = Event.deleteWhere { Event.id eq eventId }
            if (rowDetected == 0) {
                callback.onError("Event with id: $eventId does not found")
                return@transaction
            }
            callback.onSuccess("Event with id: $eventId has been remove")
            return@transaction
        }


        if (isRemoveAll == true) {
            Event.deleteAll()
            callback.onSuccess("All Events has been remove")
            return@transaction
        }

        callback.onError("Invalid EventID or DeviceID")
    }


}