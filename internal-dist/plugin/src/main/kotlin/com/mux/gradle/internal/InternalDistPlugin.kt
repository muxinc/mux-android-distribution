/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.mux.gradle.internal

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin that distributes the distribution plugins. Due to gradle limitations, this project can't
 * be distributed with everything else. It's for the other build scripts in this project to do their
 * distribution
 *
 * This plugin doesn't handle creating a publication, but it will add a version and configure artifactory
 */
class InternalDistPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.extensions.create("dist", InternalDistExtension::class.java)
    // A real version while on a tag, otherwise a dev version (different format from mux libs)
    val pluginVersion = Runtime.getRuntime().exec("git describe --tags")
      .inputStream.bufferedReader().readLines()
      .joinToString("\n").trim()
    project.version = pluginVersion
    project.logger.lifecycle("internal dist: attaching version ${project.version}")
  }
}
