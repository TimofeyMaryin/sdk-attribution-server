package org.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.model.ClickModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseClickPostgresSQL {

    val db_click by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/sdk_server_click"
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

    object Click : Table() {
        val id = integer("id").autoIncrement()
        val bundleID = varchar("bundleID", 250).nullable()
        val impressionClick = bool("impression")
        val click = bool("click")
    }

    fun init() {
        transaction(Database.connect(db_click)) {
            SchemaUtils.create(Click)
        }
    }

    fun saveClick(
        data: ClickModel,
    )  = transaction(Database.connect(db_click)) {

        Click.insert {
            it[bundleID] = data.bundleID
            it[impressionClick] = data.impressionClick
            it[click] = data.click
        }
    }

    fun getAllClick() = transaction(Database.connect(db_click)) {

        val query = Click.selectAll()

        query.map {
            ClickModel(
                id = it[Click.id],
                bundleID = it[Click.bundleID],
                impressionClick = it[Click.impressionClick],
                click = it[Click.click]
            )
        }
    }

    fun getClickByBundleID(
        bundleID: String?,
    ) = transaction(Database.connect(db_click)) {

        val query = Click.selectAll()
        query.andWhere { Click.bundleID eq bundleID }
        query.map {
            ClickModel(
                id = it[Click.id],
                bundleID = it[Click.bundleID],
                impressionClick = it[Click.impressionClick],
                click = it[Click.click]
            )
        }
    }

    fun deleteAllClicks() = transaction(Database.connect(db_click)) {
        Click.deleteAll()
    }
}