package org.example

import org.slf4j.LoggerFactory

fun main() {

    val logger = LoggerFactory.getLogger("Server")

    Server(logger).apply {
        init()
    }
}
