plugins {
  `java-gradle-plugin`
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  id("com.mux.gradle.internal.dist")
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  implementation(gradleApi())
  implementation("org.jfrog.buildinfo:build-info-extractor-gradle:5.1.11")
}

java {
  withSourcesJar()
}
dist {
  getArtifactId().set("mux-artifactory")
  getGroup().set("com.mux.gradle")
}
gradlePlugin {
  val muxArtifactory by plugins.creating {
    id = "com.mux.gradle.artifactory"
    implementationClass = "mux.artifactory.MuxArtifactoryPlugin"
    displayName = "Mux Android Artifactory Plugin"
    description = "(Thinly) Automates configuring and uploading to Artifactory, allowing you to specify both dev and " +
            "release repositories"
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
