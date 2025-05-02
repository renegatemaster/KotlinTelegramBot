plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("com.gradleup.shadow") version "8.3.0"
    application
}

group = "com.renegatemaster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.renegatemaster.englishwordsbot.telegram.TelegramKt")
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "com.renegatemaster.englishwordsbot.telegram.TelegramKt"
    }
}