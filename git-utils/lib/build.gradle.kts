/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.4/userguide/building_java_projects.html in the Gradle documentation.
 */

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  `java-library`
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")
  val mockkVersion = "1.13.8"
  testImplementation("io.mockk:mockk:${mockkVersion}")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  api("org.apache.commons:commons-math3:3.6.1")
  implementation("com.google.guava:guava:32.1.1-jre")
  implementation(gradleApi())
}

// Apply a specific Java toolchain to ease working on different environments.
java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

tasks.named<Test>("test") {
  // Use JUnit Platform for unit tests.
  useJUnitPlatform()
}
