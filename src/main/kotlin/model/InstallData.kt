package org.example.model

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class InstallData(
    val bundleId: String,
    val appName: String,
    val appVersion: String,
    val deviceId: String,
    val deviceModel: String,
    val deviceManufacturer: String,
    val androidVersion: String,
    val apiLevel: Int,
    val language: String,
    val country: String,
    val installReferrer: String? = null,
    val isFirstInstall: Boolean,
    val googleAdId: String? = null,
    val networkType: String,
    val isFromPlayStore: Boolean,
    val timestamp: Long = Instant.now().toEpochMilli(),
    val unityAdsData: String? = null,
    val id: Int? = null,
    val utmData: String? = null,
    val event: String? = null,
)