plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.21"

}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.register<Jar>("frontendJar") {
    group = "build"
    description = "Assembles a fat JAR with all dependencies for frontend."
    archiveBaseName.set("frontend")
    archiveVersion.set("")
    archiveClassifier.set("")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "org.example.MainKt" // если main() в Front-End
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it else zipTree(it)
        }
    })
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

    implementation("io.ktor:ktor-server-html-builder:2.3.0")

    implementation("ch.qos.logback:logback-classic:1.4.11")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") // Для работы с JSON
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0") // Для интеграции Ktor с kotlinx.serialization
    
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}