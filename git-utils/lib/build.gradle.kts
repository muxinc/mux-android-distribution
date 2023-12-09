plugins {
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  `java-library`
  `maven-publish`
  id("com.mux.gradle.internal.dist")
}

dist {
  getArtifactId().set("git-utils")
  getGroup().set("com.mux.gradle.git")
}

repositories {
  mavenLocal()
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

java {
  withSourcesJar()
}
//publishing {
//  // just publishing to maven local and within the monorepo for now
//  publications {
//    create<MavenPublication>("library") {
//      artifactId = "git-utils"
//      from(components["java"]) // todo - kotlin too right?
//
//      pom {
//        name = "Mux Gradle Git Utils"
//        description = "Utilities for using Git for build-related stuff in gradle tasks"
//        developers {
//          developer {
//            id = "playerandsdks"
//            name = "The Player and SDKs team @Mux"
//            email = "player@mux.com"
//          }
//        }
//      }
//    }
//  }
//}
