import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val logbackVersion = "1.2.3"
val bunqVersion = "1.13.1"
val jaxbVersion = "2.2.11"
val commonsCodecVersion = "1.14"
val jacksonVersion = "2.11.0"
val cliktVersion = "2.0.0"

plugins {
    application
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
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
