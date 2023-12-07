package mux.artifactory

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MuxArtifactoryPluginExtension {


  // TODO: Default should be to load from properties file
  abstract fun getUsername(): Property<String?>
  // TODO: Default should be to load from properties file
  abstract fun getPassword(): Property<String?>
  abstract fun getContextUrl(): Property<String?>
  abstract fun getDevRepoKey(): Property<String?>
  abstract fun getReleaseRepoKey(): Property<String?>
}