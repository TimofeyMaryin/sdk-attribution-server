package org.example.utils

object JWTConfig {
    const val SECRETE_KEY = "your-server-key"
    const val AUDIENCE = "http://192.168.1.227/hello"
    const val ISSUER = "http://192.168.1.227/"

    const val REALM = "Доступ к защищенным маршрутам"
    const val EXPIRE_TIME = 360_000

    const val NAME = "auth-jwt"
}