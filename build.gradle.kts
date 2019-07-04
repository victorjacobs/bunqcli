import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logbackVersion = "1.2.3"
val bunqVersion = "1.10.16"
val jaxbVersion = "2.2.11"
val commonsCodecVersion = "1.12"
val jacksonVersion = "2.9.9"
val cliktVersion = "2.0.0"

plugins {
    application
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "8.0.0"
}

group = "dev.vjcbs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

application {
    mainClassName = "dev.vjcbs.bunqcli.MainKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.github.bunq:sdk_java:$bunqVersion")
    implementation("javax.xml.bind:jaxb-api:$jaxbVersion")
    implementation("commons-codec:commons-codec:$commonsCodecVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.github.ajalt:clikt:$cliktVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
