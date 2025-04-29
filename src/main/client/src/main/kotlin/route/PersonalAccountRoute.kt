package org.example.route

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.Logger
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.example.model.InstallData
import org.example.utils.CountriesCode
import org.example.utils.fetchProtectedApi
import org.example.utils.format

class PersonalAccountRoute(private val logger: Logger) {


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

                        }
                        body {
                            // Верхняя часть для фильтров
                            div("header") {
                                input {
                                    id = "appNameFilter"
                                    attributes["type"] = "text"
                                    attributes["placeholder"] = "Название приложения"
                                    attributes["oninput"] = "filterByAppName()"
                                }
                                select {
                                    id = "countryFilter"
                                    option {
                                        value = CountriesCode.ALL
                                        +"Все страны"
                                    }
                                    option {
                                        value = CountriesCode.RUS
                                        +"Россия"
                                    }
                                    option {
                                        value = CountriesCode.USA
                                        +"США"
                                    }
                                    option {
                                        value = CountriesCode.GER
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
                                    val groupedData = installDataList.groupBy { it.appName }

                                    groupedData.forEach { (appName, installs) ->
                                        logger.error("installs: ${installs.forEach { it.country }}")
                                        logger.error("install: ${installs.joinToString(" | ") { it.country }}")
                                        div("card") {
                                            attributes["data-app-name"] = appName.lowercase() // <--- ключевой атрибут
                                            attributes["data-app-country"] = installs.joinToString(" | ") { it.country }
                                            attributes["onclick"] = "window.location.href='/dashboard/details/$appName'"

                                            val totalCountries = installs.groupBy { it.country }
                                            val fromPlayStore = installs.groupBy { it.isFromPlayStore }

                                            div("card-item") { h1 { +"App Name: $appName " }}
                                            div("card-item") { b { +"Total Installs: " }; +installs.size.toString() }
                                            div("card-item") { b { +"Country: " }; +totalCountries.format() }
                                            div("card-item") { b { +"From Play Store: " }; +fromPlayStore.format() }
                                        }
                                    }
                                } else {
                                    p { +"Нет данных для отображения." }
                                }
                            }
                            script {
                                unsafe {
                                    +"""
                                    function filterByAppName() {
                                        const filterValue = document.getElementById("appNameFilter").value.toLowerCase();
                                        const cards = document.querySelectorAll(".card");
                            
                                        cards.forEach(card => {
                                            const appName = card.getAttribute("data-app-name");
                                            if (!filterValue || appName.includes(filterValue)) {
                                                card.style.display = "block";
                                            } else {
                                                card.style.display = "none";
                                            }
                                        });
                                    }
                                    
                                    function applyFilters() {
                                        const countryFilter = document.getElementById('countryFilter').value;
                                        const cards = document.querySelectorAll(".card");
                                    
                                        cards.forEach(card => {
                                            const appCountries = (card.getAttribute("data-app-country") || "").split(" | ").map(s => s.trim());
                                            
                                            if (!countryFilter || appCountries.includes(countryFilter)) {
                                                card.style.display = "flex";
                                            } else {
                                                card.style.display = "none";
                                            }
                                        });
                                    }

                                    """.trimIndent()
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




