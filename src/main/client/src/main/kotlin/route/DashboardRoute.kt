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
import kotlinx.html.title
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
                            h5 { +"App Impression Click: $appImpressionClick" }
                        }
                    }

                    a(href = "/admin") { +"Back to Home" }
                }
            }

        }
    }

}