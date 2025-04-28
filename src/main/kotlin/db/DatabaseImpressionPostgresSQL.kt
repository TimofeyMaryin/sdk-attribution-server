package org.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.callback.ActionDataCallback
import org.example.db.DatabaseEventPostgreSQL.Event
import org.example.db.DatabaseEventPostgreSQL.Event.autoIncrement
import org.example.db.DatabaseEventPostgreSQL.Event.source
import org.example.db.DatabasePostgreSQL.Install
import org.example.model.ImpressionModel
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseImpressionPostgresSQL {

    val db_impression by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/sdk_server_impression"
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

    object Impression : Table() {
        val id = integer("id").autoIncrement()
        val source_app_id = varchar("source_app_id", 255).nullable()
        val campaign_name = varchar("compaign_name", 255).nullable()
        val campaign_id = varchar("campaign_id", 255).nullable()
        val creative_pack = varchar("creative_pack", 255).nullable()
        val exchange = varchar("exchange", 255).nullable()
        val ifa = varchar("ifa", 255).nullable()
        val gamer_id = varchar("gamer_id", 255).nullable()
        val game_id = varchar("game_id", 255).nullable()
        val video_orientation = varchar("video_orientation", 255).nullable()
        override val primaryKey = PrimaryKey(id)
    }

    fun init() {
        transaction(Database.connect(db_impression)) {
            SchemaUtils.create(Install)
        }
    }


    fun saveData(
        data: ImpressionModel,
        callback: ActionDataCallback
    ) = transaction(Database.connect(db_impression)) {

        Impression.insert {
            it[source_app_id] = data.source_app_id
            it[campaign_name] = data.campaign_name
            it[campaign_id] = data.campaign_id
            it[creative_pack] = data.creative_pack
            it[exchange] = data.exchange
            it[ifa] = data.ifa
            it[gamer_id] = data.gamer_id
            it[game_id] = data.game_id
            it[video_orientation] = data.video_orientation
        }
        callback.onSuccess("Data successfully saved!")
    }

    fun getAllImpression() = transaction(Database.connect(db_impression)) {
        val query = Impression.selectAll()

        query.map {
            ImpressionModel(
                source_app_id = it[Impression.source_app_id],
                campaign_name = it[Impression.campaign_name],
                campaign_id = it[Impression.campaign_id],
                creative_pack = it[Impression.creative_pack],
                exchange = it[Impression.exchange],
                ifa = it[Impression.ifa],
                gamer_id = it[Impression.gamer_id],
                game_id = it[Impression.game_id],
                video_orientation = it[Impression.video_orientation]
            )
        }
    }
}