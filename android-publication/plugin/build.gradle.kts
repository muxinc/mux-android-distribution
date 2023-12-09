plugins {
  `java-gradle-plugin`
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  groovy
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
  google()
}

dependencies {
  // Use the Kotlin JUnit 5 integration.
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  implementation("com.android.tools.build:gradle:8.1.0")
  implementation(gradleApi())
}

gradlePlugin {
  // Define the plugin
  val greeting by plugins.creating {
    id = "com.mux.gradle.android.publication"
    implementationClass = "com.mux.gradle.android.publication.AndroidPublicationPlugin"
  }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["functionalTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
  testClassesDirs = functionalTestSourceSet.output.classesDirs
  classpath = functionalTestSourceSet.runtimeClasspath
  useJUnitPlatform()
}

gradlePlugin.testSourceSets.add(functionalTestSourceSet)

tasks.named<Task>("check") {
  // Run the functional tests as part of `check`
  dependsOn(functionalTest)
}

tasks.named<Test>("test") {
  // Use JUnit Jupiter for unit tests.
  useJUnitPlatform()
}
