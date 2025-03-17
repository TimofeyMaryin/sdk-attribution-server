package org.example.cache

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection


object Cache {
    private val redisClient = RedisClient.create("redis://localhost:6379/0")
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect()
    private val syncCommands = connection.async()

    fun saveToCacheByBundleID(bundleId: String, data: String) {
        syncCommands.setex("install:$bundleId", 3600, data)
    }
    
    fun getFromCacheByBundleID(bundleId: String): String? {
        return connection.sync().get("install:${bundleId}")
    }
}