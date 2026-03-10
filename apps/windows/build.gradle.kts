plugins {
    kotlin("jvm") version "1.9.24"
    kotlin("plugin.serialization") version "1.9.24"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}

kotlin {
    jvmToolchain(17)
}

sourceSets {
    val main by getting {
        kotlin.srcDirs("desktop/src/main/kotlin", "../shared/contracts")
    }
}

application {
    mainClass.set("com.localaiproject.windows.DesktopMainKt")
}
