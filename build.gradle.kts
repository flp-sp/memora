plugins {
    java
    kotlin("jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("org.jetbrains.compose") version "1.7.0"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    // UI
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")

    // Backend
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
}

kotlin { jvmToolchain(21) }

compose.desktop {
    application {
        mainClass = "ui.MainKt"
    }
}