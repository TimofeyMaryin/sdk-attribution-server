package org.example

import org.example.db.DatabaseClickPostgresSQL
import org.example.db.DatabaseEventPostgreSQL
import org.example.db.DatabaseImpressionPostgresSQL
import org.slf4j.LoggerFactory
import org.example.db.DatabasePostgreSQL

fun main() {
    val logger = LoggerFactory.getLogger("Server")
    DatabasePostgreSQL.init()
    DatabaseEventPostgreSQL.init()
    DatabaseImpressionPostgresSQL.init()
    DatabaseClickPostgresSQL.init()

    Server(logger = logger).apply {
        init()
    }
}

