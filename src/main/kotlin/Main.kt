package org.example

import org.slf4j.LoggerFactory
import org.example.db.DatabasePostgreSQL

fun main() {
    val logger = LoggerFactory.getLogger("Server")
    DatabasePostgreSQL.init()

    Server(logger = logger).apply {
        init()
    }
}

