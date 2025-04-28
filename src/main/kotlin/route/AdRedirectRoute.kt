package org.example.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.logging.*
import org.example.callback.ActionDataCallback
import org.example.db.DatabaseClickPostgresSQL
import org.example.db.DatabaseImpressionPostgresSQL
import org.example.model.AdRedirectModel
import org.example.model.ClickModel
import org.example.model.ImpressionModel
import org.example.utils.CURRENT_URL
import org.example.utils.JWTConfig
import java.net.URI

class AdRedirectRoute(
    private val logger: Logger,
) {

    fun Route.adTracking() {
        authenticate(JWTConfig.NAME) {
            post("/click") {
                // Получаем параметры для отслеживания
                val trackingParams = buildTrackingParams() + mapOf(
                    "af_click_lookback" to "7d", // Параметры для Click
                    "af_ref" to "unityads_{auction_id}"
                )

                val appBundleId = call.receive<AdRedirectModel>()

                // Генерация ссылки для Click
                val clickTemplateUrl = "$CURRENT_URL/redirect?package_name=${appBundleId.url}".appendParams(trackingParams)

                // Генерация ссылки для Impression
                val impressionTrackingParams = buildImpressionParams() + mapOf(
                    "af_viewthrough_lookback" to "24h" // Параметры для Impression
                )
                val impressionTemplateUrl = "$CURRENT_URL/impression?package_name=${appBundleId.url}".appendParams(impressionTrackingParams)

                // Логируем результат
                logger.info("Generated Click Template: $clickTemplateUrl")
                logger.info("Generated Impression Template: $impressionTemplateUrl")

                // Отправляем обе ссылки в ответе
                call.respond(HttpStatusCode.OK, mapOf(
                    "click_url" to clickTemplateUrl,
                    "impression_url" to impressionTemplateUrl
                ))
            }
        }
    }

    fun Route.redirectToGooglePlay() {
        get("/redirect") {
            // Получаем необходимые параметры из запроса, если они нужны для аналитики
            val params = call.request.queryParameters.toMap().mapValues { it.value.firstOrNull() }

            // Собираем URL для редиректа на Google Play
            val packageName = params["package_name"] ?: "com.example.two"  // Используем дефолтное значение, если package_name не передан


            val clickData = ClickModel(bundleID = packageName, impressionClick = false, click = true)
            DatabaseClickPostgresSQL.saveClick(data = clickData)
            val playStoreUrl = "https://play.google.com/store/apps/details?id=$packageName"

            // Выполняем редирект
            call.respondRedirect(playStoreUrl.appendParams(buildTrackingParams()))
        }
    }



    fun Route.adImpression() {
        get("/impression") {
            val params = call.request.queryParameters.toMap().mapValues { it.value.firstOrNull() }


            val data = ImpressionModel(
                source_app_id = params["source_app_id"],
                campaign_name = params["campaign_name"],
                campaign_id = params["campaign_id"],
                creative_pack = params["creative_pack"],
                exchange = params["exchange"],
                ifa = params["ifa"],
                gamer_id = params["gamer_id"],
                game_id = params["game_id"],
                video_orientation = params["video_orientation"]
            )

            val clickData = ClickModel(bundleID = params["package_name"], impressionClick = true, click = false)
            DatabaseClickPostgresSQL.saveClick(data = clickData)

//            DatabaseImpressionPostgresSQL.saveData(data = data, callback = object : ActionDataCallback {
//                override fun onSuccess(msg: String) {
//                    logger.info("Impression saved: $msg")
//                }
//
//                override fun onError(e: String) {
//                    logger.error("Impression cannot save: $e")
//                }
//
//            })

            val trackingParams = buildTrackingParams() + mapOf(
                "af_viewthrough_lookback" to "24h"
            )

            logger.info("Impression tracked: $trackingParams")

            call.respond(HttpStatusCode.OK, "Impression Tracked")
        }

    }

    private fun buildTrackingParams(): Map<String, String> {
        return mapOf(
            "pid" to "unityads_int",
            "af_siteid" to "{source_app_id}",
            "c" to "{campaign_name}",
            "af_c_id" to "{campaign_id}",
            "af_ad" to "{creative_pack}",
            "af_ad_id" to "{creative_pack_id}",
            "af_adset" to "{creative_pack}",
            "af_channel" to "{exchange}",
            "advertising_id" to "{ifa}",
            "gamer_id" to "{gamer_id}",
            "game_id" to "{game_id}",
            "redirect" to "false",
            "af_ad_orientation" to "{video_orientation}",
            "idfa" to "{ifa}",
            "af_ip" to "{ip}",
            "af_ua" to "{user_agent}",
            "af_lang" to "{language}"
        )
    }

    private fun buildImpressionParams(): Map<String, String> {
        return mapOf(
            "source_app_id" to "{source_app_id}",
            "compaign_name" to "{compaign_name}",
            "campaign_id" to "{campaign_id}",
            "creative_pack" to "{creative_pack}",
            "exchange" to "{exchange}",
            "ifa" to "{ifa}",
            "gamer_id" to "{gamer_id}",
            "game_id" to "{game_id}",
            "video_orientation" to "{video_orientation}"
        )
    }

    private fun String.appendParams(params: Map<String, String>): String {
        val queryParams = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "$this&$queryParams"
    }
}
