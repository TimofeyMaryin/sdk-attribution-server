package org.example.route

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticFiles
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
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.title
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.model.InstallData
import org.example.utils.findApplicationByName
import org.example.utils.getFileRoute
import java.io.File

class StaticRoute(private val logger: Logger) {

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
                call.respondRedirect("/login?error=not_authenticated")
                return@get
            }

            val apiResponse = fetchProtectedApi(token)
            val apiText = apiResponse.bodyAsText()
            // val appData = Json.decodeFromString<List<InstallData>>(apiText).find { data -> appName == data.appName }
            val appData = Json.decodeFromString<List<InstallData>>(apiText).filter { data -> data.appName == appName }
            val countInstalls = Json.decodeFromString<List<InstallData>>(apiText).count { data -> appName == data.appName }

            if (appName == null) {
                call.respondText("App not found", status = HttpStatusCode.NotFound)
                return@get
            }

            if (appData == null) {
                call.respondText("App not found: App Data is Null", status = HttpStatusCode.NotFound)
                return@get
            }

            // call.respondFile(File(getFileRoute("dashboard.html")))

//            call.respondHtml {
//                head {
//                    title { +"Dashboard - ${appData.appName}" }
//                }
//                body {
//                    h1 { +"Details for ${appData.appName}" }
//                    p { +"Version: ${appData.appVersion}" }
//                    p { +"Device Model: ${appData.deviceModel}" }
//                    p { +"Language: ${appData.language}" }
//                    p { +"Country: ${appData.country}" }
//                    p { +"UTM: " }
//                    p { +"Installs: $countInstalls" }
//                    a(href = "/") { +"Back to Home" }
//                }
//            }


            call.respondHtml {
                head {
                    title { +"Dashboard - ${appData.size}" }
                }
                body {
                    h1 { +"Name for ${appData.first().appName}" }

                    appData.forEach { data ->
                        div {
                           h3 { +"App Name: ${data.appName}" }

                            h5 { +"App Bundle: ${data.bundleId}" }
                            h5 { +"App UTM DATA: ${data.utmData}" }
                            h5 { +"App Install Referer: ${data.installReferrer}" }
                        }
                    }

                    a(href = "/") { +"Back to Home" }
                }
            }

        }
    }

}