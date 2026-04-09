plugins {
    alias(libs.plugins.kotlinJvm)
    kotlin("plugin.serialization") version "2.0.0"
    application
}

dependencies {
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    implementation(libs.logback)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serverNegotiation)
    implementation(libs.ktor.serverCors)
    implementation("org.mindrot:jbcrypt:0.4")
    implementation(libs.ktor.serverLogging)
    implementation(project(":composeApp"))
    implementation(project(":shared"))
}

application {
    mainClass.set("org.kollmir.dialogram.ServerKt")
}