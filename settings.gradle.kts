plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"

}
rootProject.name = "SDK_Server"
//include("src:main:client")
//findProject(":src:main:client")?.name = "client"

include(":client")
project(":client").projectDir = file("src/main/client")

//include("data")