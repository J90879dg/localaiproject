plugins {
    kotlin("jvm") version "1.9.24"
    application
}

repositories {
    mavenCentral()
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
