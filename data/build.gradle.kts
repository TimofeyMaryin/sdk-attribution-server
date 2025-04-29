plugins {
    kotlin("jvm")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") // JSON сериализацияъ
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}