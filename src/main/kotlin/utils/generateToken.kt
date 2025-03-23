package org.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

fun generateToken(username: String): String {
    return JWT
        .create()
        .withAudience(JWTConfig.AUDIENCE)
        .withIssuer(JWTConfig.ISSUER)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + JWTConfig.EXPIRE_TIME))
        .sign(Algorithm.HMAC256(JWTConfig.SECRETE_KEY))
}