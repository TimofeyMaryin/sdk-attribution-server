package org.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.example.callback.ActionDataCallback
import org.example.db.DatabaseEventPostgreSQL.db_event
import org.example.model.InstallData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DatabasePostgreSQL {

    private val db by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/sdk_server"
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

    object Install : Table() {
        val id = integer("id").autoIncrement()
        val bundleId = varchar("bundleId", 100)
        val deviceId = varchar("deviceId", 100)
        val appName = varchar("appName", 100)
        val appVersion = varchar("appVersion", 100)
        val deviceModel = varchar("deviceModel", 100)
        val deviceManufacturer = varchar("deviceManufacturer", 100)
        val androidVersion = varchar("androidVersion", 100)
        val apiLevel = integer("apiLevel")
        val language = varchar("language", 100)
        val country = varchar("country", 100)
        val installReferrer = varchar("installReferrer", 100).nullable()
        val isFirstInstall = bool("isFirstInstall")
        val googleAdId = varchar("googleAdId", 100).nullable()
        val networkType = varchar("networkType", 100)
        val isFromPlayStore = bool("isFromPlayStore")
        val timestamp = long("timestamp")
        val utmData = varchar("utmData", 255).nullable()
        val unityAdsData = varchar("unityAdsData", 1000).nullable()
        val events = text("event").nullable()
        override val primaryKey = PrimaryKey(id)
    }

    fun init() {
        transaction(Database.connect(db)) {
            SchemaUtils.create(Install)
        }
    }

    fun saveInstall(
        data: InstallData,
        callback: ActionDataCallback
    ) = transaction(Database.connect(db)) {
        val existingInstall = Install.select {
            (Install.bundleId eq data.bundleId) and
                    (Install.appName eq data.appName) and
                    (Install.deviceId eq data.deviceId) and
                    (Install.androidVersion eq data.androidVersion) and
                    (Install.deviceManufacturer eq data.deviceManufacturer)
        }.singleOrNull()

        println("SAVE_INSTALL:  existingInstall: $existingInstall")
        println("SAVE_INSTALL: $data")

        if (existingInstall != null) {
            callback.onError("Data already exist: $data")
            return@transaction
        }
        Install.insert {
            it[bundleId] = data.bundleId
            it[deviceId] = data.deviceId
            it[appName] = data.appName
            it[appVersion] = data.appVersion
            it[deviceModel] = data.deviceModel
            it[deviceManufacturer] = data.deviceManufacturer
            it[androidVersion] = data.androidVersion
            it[apiLevel] = data.apiLevel
            it[language] = data.language
            it[country] = data.country
            it[installReferrer] = data.installReferrer
            it[isFirstInstall] = data.isFirstInstall
            it[googleAdId] = data.googleAdId
            it[networkType] = data.networkType
            it[isFromPlayStore] = data.isFromPlayStore
            it[timestamp] = data.timestamp
            it[unityAdsData] = data.unityAdsData
            it[utmData] = data.utmData
        }
        callback.onSuccess("Data successfull save: $data")
    }

    fun getAllInstalls(
        appName: String? = null,
        bundleID: String? = null,
        fromData: Long? = null,
        toData: Long? = null,
        fromApiLevel: Int? = null,
        toApiLevel: Int? = null,
        fromAndroidApiLevel: Int? = null,
        toAndroidApiLevel: Int? = null,
        country: String? = null,
    ) = transaction(Database.connect(db)) {
        val query = Install.selectAll()

        if (appName != null) {
            query.andWhere { Install.appName eq appName }
        }

        if (bundleID != null) {
            query.andWhere { Install.bundleId eq bundleID }
        }

        if (fromData != null) {
            query.andWhere { Install.timestamp greaterEq fromData }
        }

        if (toData != null) {
            query.andWhere { Install.timestamp lessEq toData }
        }

        if (fromApiLevel != null) {
            query.andWhere { Install.apiLevel greaterEq fromApiLevel }
        }

        if (toApiLevel != null) {
            query.andWhere { Install.apiLevel lessEq toApiLevel }
        }

        if (fromAndroidApiLevel != null) {
            query.andWhere { Install.androidVersion.castTo<Int>(IntegerColumnType()) greaterEq fromAndroidApiLevel }
        }

        if (toAndroidApiLevel != null) {
            query.andWhere { Install.androidVersion.castTo<Int>(IntegerColumnType()) lessEq toAndroidApiLevel }
        }

        if (country != null) {
            query.andWhere { Install.country eq country }
        }

        query.map {
            val installData = InstallData(
                bundleId = it[Install.bundleId],
                appName = it[Install.appName],
                appVersion = it[Install.appVersion],
                deviceId = it[Install.deviceId],
                deviceModel = it[Install.deviceModel],
                deviceManufacturer = it[Install.deviceManufacturer],
                androidVersion = it[Install.androidVersion],
                apiLevel = it[Install.apiLevel],
                language = it[Install.language],
                country = it[Install.country],
                installReferrer = it[Install.installReferrer],
                isFirstInstall = it[Install.isFirstInstall],
                googleAdId = it[Install.googleAdId],
                networkType = it[Install.networkType],
                isFromPlayStore = it[Install.isFromPlayStore],
                timestamp = it[Install.timestamp],
                unityAdsData = it[Install.unityAdsData],
                id = it[Install.id],
                utmData = it[Install.utmData],
                event = null,
            )

            val events = transaction(Database.connect(db_event)) {
                DatabaseEventPostgreSQL.Event.select {
                    (DatabaseEventPostgreSQL.Event.deviceId eq installData.deviceId) and
                            (DatabaseEventPostgreSQL.Event.bundleId eq installData.bundleId)
                }
                    .map { it[DatabaseEventPostgreSQL.Event.event] }
            }.toString()

            installData.copy(event = events)
        }
    }



    fun deleteInstallById(deviceID: String?) = transaction(Database.connect(db)) {
        if (deviceID == null) {
            Install.deleteAll()
            DatabaseEventPostgreSQL.deleteEventByDeviceID(null)
            return@transaction
        }

        val rowsDetect = Install.deleteWhere { Install.deviceId eq deviceID }
        DatabaseEventPostgreSQL.deleteEventByDeviceID(deviceID)
        if (rowsDetect == 0) {
            println("No install with ID $deviceID")
        } else {
            println("Install with ID $deviceID deleted")
        }
    }


}
