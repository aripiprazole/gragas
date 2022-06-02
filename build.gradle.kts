/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

plugins {
  kotlin("jvm") version "1.6.21"
  application
}

group = "com.gabrielleeg1"
version = "1.0"

repositories {
  mavenCentral()
  maven("https://m2.dv8tion.net/releases")
}

dependencies {
  implementation("dev.kord:kord-core:0.8.0-M14") {
    capabilities {
      requireCapability("dev.kord:core-voice:0.8.0-M14")
    }
  }
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.2")
  implementation("com.sedmelluq:lavaplayer:1.3.77")
  implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

  runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
  runtimeOnly("org.fusesource.jansi:jansi:2.4.0")
}

application {
  mainClass.set("gragas.Main")
}

tasks {
  compileKotlin {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
  }
}
