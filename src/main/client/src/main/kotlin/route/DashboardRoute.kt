package org.example.route

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.model.InstallData
import org.example.utils.URL_TO_SERVER

class DashboardRoute {


    fun Route.adminRoute() {
        get("/admin") {
            val token = call.request.cookies["jwt_token"]

            if (token == null) {
                call.respondRedirect("/login?error=not_authenticated")
                return@get
            }

            try {
                val apiResponse = fetchProtectedApi(token)
                val apiText = apiResponse.bodyAsText()

                if (apiResponse.status == HttpStatusCode.OK) {
                    val installDataList = Json.decodeFromString<List<InstallData>>(apiText)

                    call.respondHtml {
                        head {
                            title { +"Админ-панель" }
                            style {
                                unsafe {
                                    +"""
                                body {
                                    margin: 0;
                                    font-family: Arial, sans-serif;
                                }
                                .header {
                                    position: sticky;
                                    top: 0;
                                    background-color: #ffffff;
                                    z-index: 1000;
                                    padding: 1rem;
                                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                                    display: flex;
                                    gap: 1rem;
                                    align-items: center;
                                }
                                .header input, .header select, .header button {
                                    padding: 0.5rem;
                                    border: 1px solid #ddd;
                                    border-radius: 6px;
                                    font-size: 1rem;
                                }
                                .header button {
                                    background-color: #007bff;
                                    color: white;
                                    border: none;
                                    cursor: pointer;
                                    transition: background-color 0.2s;
                                }
                                .header button:hover {
                                    background-color: #0056b3;
                                }
                                .card-container {
                                    display: flex;
                                    flex-direction: column;
                                    gap: 1rem;
                                    padding: 1rem;
                                }
                                .card {
                                    width: 100%;
                                    background-color: #ffffff;
                                    border-radius: 12px;
                                    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                                    padding: 1rem;
                                    transition: transform 0.2s, box-shadow 0.2s;
                                    cursor: pointer;
                                    display: flex;
                                    flex-direction: column;
                                    gap: 0.5rem;
                                }
                                .card:hover {
                                    transform: translateY(-2px);
                                    box-shadow: 0 6px 10px rgba(0, 0, 0, 0.15);
                                }
                                .card-item {
                                    font-size: 0.95rem;
                                    color: #333;
                                    word-break: break-word;
                                }
                                .card-item b {
                                    font-weight: bold;
                                    color: #555;
                                }
                                """
                                }
                            }
                            script {
                                unsafe {
                                    +"""
                                function applyFilters() {
                                    const appNameFilter = document.getElementById('appNameFilter').value;
                                    const countryFilter = document.getElementById('countryFilter').value;

                                    alert('Фильтры применены: AppName = ' + appNameFilter + ', Country = ' + countryFilter);
                                    // Здесь можно реализовать логику фильтрации с отправкой запроса на сервер
                                }
                                """
                                }
                            }
                        }
                        body {
                            // Верхняя часть для фильтров
                            div("header") {
                                input {
                                    id = "appNameFilter"
                                    attributes["type"] = "text"
                                    attributes["placeholder"] = "Фильтр по appName"
                                }
                                select {
                                    id = "countryFilter"
                                    option {
                                        value = ""
                                        +"Все страны"
                                    }
                                    option {
                                        value = "RU"
                                        +"Россия"
                                    }
                                    option {
                                        value = "US"
                                        +"США"
                                    }
                                    option {
                                        value = "DE"
                                        +"Германия"
                                    }
                                }
                                button {
                                    attributes["onclick"] = "applyFilters()"
                                    +"Применить фильтры"
                                }
                            }

                            // Карточки данных
                            div("card-container") {
                                if (installDataList.isNotEmpty()) {
                                    installDataList.forEach { data ->
                                        div("card") {
                                            attributes["onclick"] =
                                                "window.location.href='/dashboard/details/${data.appName}'"

                                            div("card-item") { b { +"App Name: " }; +data.appName }
                                            div("card-item") { b { +"App Version: " }; +data.appVersion }
                                            div("card-item") { b { +"UTM Data: " }; +(data.utmData?.toString() ?: "N/A") }
                                            div("card-item") { b { +"Device Model: " }; +data.deviceModel }
                                            div("card-item") { b { +"Language: " }; +data.language }
                                            div("card-item") { b { +"Country: " }; +data.country }
                                            div("card-item") { b { +"From Play Store: " }; +data.isFromPlayStore.toString() }
                                            div("card-item") { b { +"Event: " }; +(data.event?.toString() ?: "N/A") }
                                        }
                                    }
                                } else {
                                    p { +"Нет данных для отображения." }
                                }
                            }
                        }
                    }
                } else {
                    call.respondRedirect("/login?error=unauthorized")
                }
            } catch (e: Exception) {
                call.respondRedirect("/login?error=api_unreachable")
            }
        }
    }

}


suspend fun fetchProtectedApi(token: String): HttpResponse {
    val client = HttpClient()
    return client.get("${URL_TO_SERVER}apps") {
        header("Authorization", "Bearer $token")
    }
}