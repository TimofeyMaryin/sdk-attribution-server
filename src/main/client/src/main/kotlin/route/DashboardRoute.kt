package org.example.route

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.util.logging.Logger
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h3
import kotlinx.html.h5
import kotlinx.html.h6
import kotlinx.html.head
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.unsafe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.model.ClickModel
import org.example.model.InstallData
import org.example.utils.fetchProtectedApi
import org.example.utils.fetchProtectedImpressionClick
import org.example.utils.getFileRoute
import java.io.File
import kotlin.math.log

class DashboardRoute(private val logger: Logger) {

    private data class SourceStats(
        val source: String,
        val installs: Int = 0,
        val impressions: Int = 0,
        val clicks: Int = 0,
        val campaigns: MutableMap<String, CampaignStats> = mutableMapOf()
    )

    private data class CampaignStats(
        val campaign: String,
        var installs: Int = 0,
        var impressions: Int = 0,
        var clicks: Int = 0,
        val mediums: MutableSet<String> = mutableSetOf(),
        val contents: MutableSet<String> = mutableSetOf(),
        var siteId: String = "-",
        var creative: String = "-",
        var trackerToken: String = "-",
        var trackerName: String = "-"
    )

    private fun parseUtmData(utmString: String): Map<String, String> {
        return utmString.split("&").associate {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1].replace("%20", " ") else parts[0] to ""
        }
    }

    private fun parseInstallReferrer(referrer: String): Map<String, String> {
        return referrer.split("&").associate {
            val parts = it.split("=")
            if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
        }
    }

    private fun aggregateStats(appData: List<InstallData>, appImpressionClick: List<ClickModel>): Map<String, SourceStats> {
        val stats = mutableMapOf<String, SourceStats>()
        
        appData.forEach { data ->
            val referrerData = parseInstallReferrer(data.installReferrer!!)
            val utmData = parseUtmData(data.utmData!!)

            // Приоритет: referrer > utm_source > direct
            val source: String
            val campaign: String
            val siteId: String
            val creative: String
            val trackerToken: String
            val trackerName: String

            if (referrerData.isNotEmpty()) {
                source = referrerData["media_source"] ?: "referrer"
                campaign = referrerData["campaign"] ?: referrerData["utm_campaign"] ?: "unknown"
                siteId = referrerData["site_id"] ?: "-"
                creative = referrerData["creative"] ?: "-"
                trackerToken = referrerData["tracker_token"] ?: "-"
                trackerName = referrerData["tracker_name"] ?: "-"
            } else if (utmData["utm_source"] != null) {
                source = utmData["utm_source"] ?: "direct"
                campaign = utmData["utm_campaign"] ?: "organic"
                siteId = utmData["site_id"] ?: "-"
                creative = utmData["creative"] ?: "-"
                trackerToken = utmData["tracker_token"] ?: "-"
                trackerName = utmData["tracker_name"] ?: "-"
            } else {
                source = "direct"
                campaign = "organic"
                siteId = "-"
                creative = "-"
                trackerToken = "-"
                trackerName = "-"
            }

            val sourceStats = stats.getOrPut(source) { SourceStats(source) }
            val campaignStats = sourceStats.campaigns.getOrPut(campaign) {
                CampaignStats(campaign)
            }
            campaignStats.installs++
            campaignStats.siteId = siteId
            campaignStats.creative = creative
            campaignStats.trackerToken = trackerToken
            campaignStats.trackerName = trackerName
            utmData["utm_medium"]?.let { campaignStats.mediums.add(it) }
            utmData["utm_content"]?.let { campaignStats.contents.add(it) }
        }

        // Добавляем данные о показах и кликах
        appImpressionClick.forEach { click ->
            stats.values.forEach { sourceStats ->
                sourceStats.campaigns.values.forEach { campaignStats ->
                    campaignStats.impressions += if (click.impressionClick) 1 else 0
                    campaignStats.clicks += if (click.click) 1 else 0
                }
            }
        }

        return stats
    }

    fun Route.start() {
        get("/static/start") {
            call.respondFile(File(getFileRoute("index.html")))
        }
    }

    fun Route.dashboardApplication() {
        get("/dashboard/details/{appName}") {
            val token = call.request.cookies["jwt_token"]
            val appName = call.parameters["appName"]

            if (token == null) {
                logger.error("Token is Invalidate! Please Check your JWT token")
                call.respondRedirect("/login?error=not_authenticated")
                return@get
            }

            val apiResponse = fetchProtectedApi(token)
            val apiText = apiResponse.bodyAsText()
            val appData = Json.decodeFromString<List<InstallData>>(apiText).filter { data -> data.appName == appName }

            val clickResponse = fetchProtectedImpressionClick(token, bundleId = appData.first().bundleId)
            val apiClick = clickResponse.bodyAsText()

            if (apiClick.contains("List is Empty")) {
                call.respondText("Impression and Simple Click is Empty! This is impossible! Please check your application!", status = HttpStatusCode.NotFound)
                return@get
            }

            val appImpressionClick = Json.decodeFromString<List<ClickModel>>(apiClick)

            if (appName == null) {
                logger.error("App Name is Null! App not found")
                call.respondText("App not found", status = HttpStatusCode.NotFound)
                return@get
            }

            if (appData.isEmpty()) {
                logger.error("App Data not found!")
                call.respondText("App not found: App Data is Null", status = HttpStatusCode.NotFound)
                return@get
            }

            call.respondHtml {
                head {
                    title { +"Dashboard - ${appData.first().appName}" }
                    style {
                        unsafe {
                            +"""
                            body {
                                font-family: Arial, sans-serif;
                                margin: 0;
                                padding: 20px;
                                background-color: #f5f5f5;
                            }
                            .container {
                                max-width: 1200px;
                                margin: 0 auto;
                            }
                            .app-card {
                                background: white;
                                border-radius: 8px;
                                padding: 20px;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                margin-bottom: 20px;
                            }
                            .app-header {
                                display: flex;
                                justify-content: space-between;
                                align-items: center;
                                margin-bottom: 20px;
                                padding-bottom: 10px;
                                border-bottom: 1px solid #eee;
                            }
                            .app-title {
                                color: #333;
                                margin: 0;
                            }
                            .back-link {
                                color: #0066cc;
                                text-decoration: none;
                                padding: 8px 16px;
                                border-radius: 4px;
                                background: #f0f7ff;
                            }
                            .back-link:hover {
                                background: #e0e0e0;
                            }
                            .info-section {
                                margin-bottom: 15px;
                            }
                            .info-label {
                                color: #666;
                                font-size: 14px;
                                margin-bottom: 5px;
                            }
                            .info-value {
                                color: #333;
                                font-size: 16px;
                                font-weight: 500;
                            }
                            """
                        }
                    }
                }
                body {
                    div(classes = "container") {
                        div(classes = "app-card") {
                            div(classes = "app-header") {
                                h1(classes = "app-title") { +appData.first().appName }
                                a(href = "/admin", classes = "back-link") { +"← Back to Home" }
                            }
                            
                            div(classes = "info-section") {
                                div(classes = "info-label") { +"Bundle ID" }
                                div(classes = "info-value") { +appData.first().bundleId }
                            }
                            
                            div(classes = "info-section") {
                                div(classes = "info-label") { +"UTM Data" }
                                div(classes = "info-value") { +appData.first().utmData!! }
                            }
                            
                            div(classes = "info-section") {
                                div(classes = "info-label") { +"Install Referrer" }
                                div(classes = "info-value") { +appData.first().installReferrer!! }
                            }
                            
                            div(classes = "info-section") {
                                div(classes = "info-label") { +"Total Impressions & Clicks" }
                                div(classes = "info-value") { +appImpressionClick.size.toString() }
                            }

                            div(classes = "data-table") {
                                style {
                                    unsafe {
                                        +"""
                                        .data-table {
                                            background: white;
                                            border-radius: 8px;
                                            padding: 20px;
                                            margin-top: 20px;
                                            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                                            overflow-x: auto;
                                        }
                                        .table-wrapper {
                                            min-width: 100%;
                                            overflow-x: auto;
                                        }
                                        .table-title {
                                            color: #333;
                                            font-size: 18px;
                                            margin-bottom: 15px;
                                            padding-bottom: 10px;
                                            border-bottom: 1px solid #eee;
                                            position: sticky;
                                            left: 0;
                                            background: white;
                                        }
                                        table {
                                            min-width: 1200px;
                                            width: 100%;
                                            border-collapse: collapse;
                                        }
                                        th {
                                            position: sticky;
                                            top: 0;
                                            background-color: #f8f9fa;
                                            z-index: 1;
                                            white-space: nowrap;
                                        }
                                        th, td {
                                            padding: 12px 15px;
                                            text-align: left;
                                            border-bottom: 1px solid #eee;
                                        }
                                        td {
                                            white-space: nowrap;
                                            max-width: 300px;
                                            overflow: hidden;
                                            text-overflow: ellipsis;
                                        }
                                        th {
                                            background-color: #f8f9fa;
                                            color: #333;
                                            font-weight: 600;
                                        }
                                        tr:hover {
                                            background-color: #f5f5f5;
                                        }
                                        .source-cell {
                                            position: sticky;
                                            left: 0;
                                            background: inherit;
                                            color: #0066cc;
                                            font-weight: 500;
                                            z-index: 1;
                                        }
                                        .metric-cell {
                                            text-align: right;
                                            font-family: monospace;
                                        }
                                        .campaign-row {
                                            background-color: #f8f9fa;
                                            font-size: 0.95em;
                                        }
                                        .campaign-cell {
                                            padding-left: 30px;
                                        }
                                        .total-row {
                                            font-weight: 600;
                                            background-color: #f0f7ff;
                                        }
                                        .total-row td:first-child {
                                            position: sticky;
                                            left: 0;
                                            background: #f0f7ff;
                                        }
                                        """
                                    }
                                }

                                div(classes = "table-title") { +"Traffic Sources Breakdown" }
                                
                                div(classes = "table-wrapper") {
                                    table {
                                        thead {
                                            tr {
                                                th { +"Source / Campaign" }
                                                th { +"Site ID" }
                                                th { +"Creative" }
                                                th { +"Tracker Token" }
                                                th { +"Tracker Name" }
                                                th { +"Installs" }
                                                th { +"Impressions" }
                                                th { +"Clicks" }
                                                th { +"CTR" }
                                                th { +"CVR" }
                                            }
                                        }
                                        tbody {
                                            val stats = aggregateStats(appData, appImpressionClick)
                                            
                                            // Добавляем строку с общими итогами
                                            tr(classes = "total-row") {
                                                td { +"Totals" }
                                                td { +"-" }
                                                td { +"-" }
                                                td { +"-" }
                                                td { +"-" }
                                                td(classes = "metric-cell") { 
                                                    +"${stats.values.sumOf { it.campaigns.values.sumOf { it.installs } }}"
                                                }
                                                td(classes = "metric-cell") {
                                                    +"${stats.values.sumOf { it.campaigns.values.sumOf { it.impressions } }}"
                                                }
                                                td(classes = "metric-cell") {
                                                    +"${stats.values.sumOf { it.campaigns.values.sumOf { it.clicks } }}"
                                                }
                                                td(classes = "metric-cell") {
                                                    val totalImpressions = stats.values.sumOf { it.campaigns.values.sumOf { it.impressions } }.toDouble()
                                                    val totalClicks = stats.values.sumOf { it.campaigns.values.sumOf { it.clicks } }.toDouble()
                                                    +"${String.format("%.2f%%", if (totalImpressions > 0) (totalClicks / totalImpressions) * 100 else 0.0)}"
                                                }
                                                td(classes = "metric-cell") {
                                                    val totalClicks = stats.values.sumOf { it.campaigns.values.sumOf { it.clicks } }.toDouble()
                                                    val totalInstalls = stats.values.sumOf { it.campaigns.values.sumOf { it.installs } }.toDouble()
                                                    +"${String.format("%.2f%%", if (totalClicks > 0) (totalInstalls / totalClicks) * 100 else 0.0)}"
                                                }
                                            }
                                            
                                            // Добавляем данные по каждому источнику и кампании
                                            stats.forEach { (source, sourceStats) ->
                                                // Строка источника
                                                tr {
                                                    td(classes = "source-cell") { +source }
                                                    td { +sourceStats.campaigns.values.first().siteId }
                                                    td { +sourceStats.campaigns.values.first().creative }
                                                    td { +sourceStats.campaigns.values.first().trackerToken }
                                                    td { +sourceStats.campaigns.values.first().trackerName }
                                                    td(classes = "metric-cell") {
                                                        +"${sourceStats.campaigns.values.sumOf { it.installs }}"
                                                    }
                                                    td(classes = "metric-cell") {
                                                        +"${sourceStats.campaigns.values.sumOf { it.impressions }}"
                                                    }
                                                    td(classes = "metric-cell") {
                                                        +"${sourceStats.campaigns.values.sumOf { it.clicks }}"
                                                    }
                                                    td(classes = "metric-cell") {
                                                        val impressions = sourceStats.campaigns.values.sumOf { it.impressions }.toDouble()
                                                        val clicks = sourceStats.campaigns.values.sumOf { it.clicks }.toDouble()
                                                        +"${String.format("%.2f%%", if (impressions > 0) (clicks / impressions) * 100 else 0.0)}"
                                                    }
                                                    td(classes = "metric-cell") {
                                                        val clicks = sourceStats.campaigns.values.sumOf { it.clicks }.toDouble()
                                                        val installs = sourceStats.campaigns.values.sumOf { it.installs }.toDouble()
                                                        +"${String.format("%.2f%%", if (clicks > 0) (installs / clicks) * 100 else 0.0)}"
                                                    }
                                                }
                                                
                                                // Строки кампаний
                                                sourceStats.campaigns.values.forEach { campaign ->
                                                    tr(classes = "campaign-row") {
                                                        td(classes = "campaign-cell") { +campaign.campaign }
                                                        td { +campaign.siteId }
                                                        td { +campaign.creative }
                                                        td { +campaign.trackerToken }
                                                        td { +campaign.trackerName }
                                                        td(classes = "metric-cell") { +"${campaign.installs}" }
                                                        td(classes = "metric-cell") { +"${campaign.impressions}" }
                                                        td(classes = "metric-cell") { +"${campaign.clicks}" }
                                                        td(classes = "metric-cell") {
                                                            +"${String.format("%.2f%%", if (campaign.impressions > 0) (campaign.clicks.toDouble() / campaign.impressions.toDouble()) * 100 else 0.0)}"
                                                        }
                                                        td(classes = "metric-cell") {
                                                            +"${String.format("%.2f%%", if (campaign.clicks > 0) (campaign.installs.toDouble() / campaign.clicks.toDouble()) * 100 else 0.0)}"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}