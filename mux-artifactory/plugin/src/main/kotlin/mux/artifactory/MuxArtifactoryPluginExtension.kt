package mux.artifactory

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MuxArtifactoryPluginExtension {

  abstract fun getUsername(): Property<String?>
  abstract fun getPassword(): Property<String?>
  abstract fun getContextUrl(): Property<String?>
  abstract fun getDevRepoKey(): Property<String?>
  abstract fun getReleaseRepoKey(): Property<String?>
}