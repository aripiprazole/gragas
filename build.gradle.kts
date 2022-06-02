/*
 * The contents of this file is free and unencumbered software released into the
 * public domain. For more information, please refer to <http://unlicense.org/>
 */

import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  kotlin("multiplatform") version "1.7.0-RC"
  application
}

group = "com.gabrielleeg1"
version = "1.0"

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    withJava()
    compilations.all {
      kotlinOptions.jvmTarget = "16"
    }
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
      testLogging.showStandardStreams = true
      testLogging.exceptionFormat = TestExceptionFormat.FULL
    }
  }
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation("dev.kord:kord-core:0.8.0-M14")
      }
    }
    val jvmTest by getting
  }
}

application {
  mainClass.set("gragas.Main")
}
