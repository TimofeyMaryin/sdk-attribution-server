package org.example.model


data class ImpressionModel(
    val id: Int? = null,
    val source_app_id: String?,
    val campaign_name: String?,
    val campaign_id: String?,
    val creative_pack: String?,
    val exchange: String?,
    val ifa: String?,
    val gamer_id: String?,
    val game_id: String?,
    val video_orientation: String?
)
