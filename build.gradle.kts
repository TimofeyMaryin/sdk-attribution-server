plugins {
    application
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-call-logging:2.3.0")
    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("io.ktor:ktor-client-core:2.3.0") // Клиент Ktor
    implementation("io.ktor:ktor-client-cio:2.3.0")  // HTTP клиент Ktor
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0") // Поддержка JSON
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0") // Поддержка JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") // JSON сериализацияъ
    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("org.jetbrains.exposed:exposed-core:0.41.1")  // ORM
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.5.1") // Драйвер PostgreSQL
    implementation("com.zaxxer:HikariCP:5.0.1") // Connection Pool

    implementation("io.lettuce:lettuce-core:6.2.6.RELEASE") // Redis client library
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2") // JSON support for Redis
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}