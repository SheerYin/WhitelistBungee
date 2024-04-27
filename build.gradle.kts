import java.text.SimpleDateFormat
import java.util.*

plugins {
    kotlin("jvm") version "1.9.23"
}

var pluginVersion = SimpleDateFormat("yyyy.MM.dd").format(Date()) + "-SNAPSHOT"
group = "io.github.yin.whitelistbungee"
version = ""

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.register("writePluginVersion") {
    doLast {
        file("src/main/resources/bungee.yml").apply {
            writeText(readText().replace("{pluginVersion}", pluginVersion))
        }
    }
}

tasks.register("restorePluginVersion") {
    doLast {
        file("src/main/resources/bungee.yml").apply {
            writeText(readText().replace(pluginVersion, "{pluginVersion}"))
        }
    }
}

tasks.named("compileJava") {
    dependsOn("writePluginVersion")
}

tasks.named("build") {
    finalizedBy("restorePluginVersion")
}

kotlin {
    jvmToolchain(17)
}