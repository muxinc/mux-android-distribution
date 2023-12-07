package mux.artifactory

import org.gradle.api.Project
import org.gradle.api.provider.Property

abstract class MuxArtifactoryPluginExtension {

  internal lateinit var project: Project
  internal lateinit var plugin: MuxArtifactoryPlugin

  abstract fun getUsername(): Property<String?>
  abstract fun getPassword(): Property<String?>
  abstract fun getContextUrl(): Property<String?>
  abstract fun getDevRepoKey(): Property<String?>
  abstract fun getReleaseRepoKey(): Property<String?>

  fun publishToProdIf(block: ( ) -> Boolean)  {
    plugin.publishToProd = block
  }
}
