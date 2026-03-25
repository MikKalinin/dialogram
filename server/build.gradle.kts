plugins {
    alias(libs.plugins.kotlinJvm)
    application
}

dependencies {
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":composeApp"))
}

application {
    mainClass.set("org.kollmir.dialogram.ServerKt")
}