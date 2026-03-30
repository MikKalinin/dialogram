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
    implementation(libs.ktor.serverNegotiation)
    implementation(project(":composeApp"))
    implementation(project(":shared"))
}

application {
    mainClass.set("org.kollmir.dialogram.ServerKt")
}