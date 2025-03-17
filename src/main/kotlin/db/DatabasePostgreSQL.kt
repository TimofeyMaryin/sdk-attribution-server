package org.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object DatabasePostgreSQL {

    private val db by lazy {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://localhost:5432/sdl_server"
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
        override val primaryKey = PrimaryKey(id)
    }

    fun init() {
        transaction(Database.connect(db)) {
            SchemaUtils.create(Install)
        }
    }

    fun saveInstall(data: InstallData) = transaction(Database.connect(db)) {
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
    }

    fun getInstallsByAppName(appName: String) = transaction(Database.connect(db)) {
        Install.select { Install.appName eq appName }.map {
            InstallData(
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
            )
        }
    }

    fun getInstallsByBundleID(bundleID: String) = transaction(Database.connect(db)) {
        Install.select { Install.bundleId eq bundleID }.map {
            InstallData(
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
            )
        }
    }

    fun getAllInstalls() = transaction(Database.connect(db)) {
        Install.selectAll().map {
            InstallData(
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
                utmData = it[Install.utmData]
            )
        }
    }

    fun deleteInstallByID(installID: Int) = transaction(Database.connect(db)) {
        val rowsDetect = Install.deleteWhere { Install.id eq installID }
        if (rowsDetect == 0) {
            println("No install with ID $installID")
        } else {
            println("Install with ID $installID deleted")
        }
    }
}
