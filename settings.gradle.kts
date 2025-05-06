plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"

}
rootProject.name = "SDK_Server"

include(":client")
include(":main")
project(":client").projectDir = file("src/main/client")