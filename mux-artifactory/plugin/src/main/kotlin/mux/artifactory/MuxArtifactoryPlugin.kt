/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package mux.artifactory

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin
import org.jfrog.gradle.plugin.artifactory.dsl.ArtifactoryPluginConvention

/**
 * A plugin that handles authentication and config for artifactory
 * To authenticate, prefer using `local.properties`. The following keys are required:
 * * `artifactory_username`
 * * `artifactory_password`
 *
 * You can also put then in your environment variables, prefixed with `ORG_GRADLE_PROJECT`.
 * For debugging purposes they can also be defined literally by the DSL
 */
class MuxArtifactoryPlugin: Plugin<Project> {

    private lateinit var extension: MuxArtifactoryPluginExtension
    private lateinit var project: Project

    internal lateinit var publishToProd: () -> Boolean

    override fun apply(project: Project) {
      this.project = project
      this.extension = project.extensions.create("muxArtifactory", MuxArtifactoryPluginExtension::class.java)
      this.extension.plugin = this
      this.extension.project = project
      val plugin = project.plugins.apply(ArtifactoryPlugin::class.java)
//      project.plugins.apply<ArtifactoryPlugin::class.java>() {
//
//      }

      val artifactoryExt = project.extensions.findByType(ArtifactoryPluginConvention::class.java)
        ?: throw GradleException("Unexpected: Artifactory plugin didn't apply")

      val artifactoryCredentials: ArtifactoryCredentials
      if (extension.getUsername().get().isNullOrBlank() || extension.getPassword().get().isNullOrBlank()) {
        artifactoryCredentials = artifactoryCredentialsFromLocalProperties(project)
      } else {
        val username = extension.getUsername().get()
        val password = extension.getPassword().get()
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") // it's necessary, wtf
        artifactoryCredentials = ArtifactoryCredentials(username!!, password!!)
      }

      artifactoryExt.artifactory {
        it.setContextUrl(extension.getContextUrl())

        it.publish {  publisherConfig ->
          publisherConfig.repository { repository ->
            if (publishToProd()) {
              repository.setRepoKey(extension.getReleaseRepoKey().get())
              repository.setUsername(artifactoryCredentials.username)
              repository.setPassword(artifactoryCredentials.password)
            }
          }
          publisherConfig.defaults { artifactoryTask ->
            // Todo: hopefully this is adequate without creating tons of extra publications
            artifactoryTask.publications("ALL_PUBLICATIONS")
          }
        }
      }

      // TODO: must configure artifactory after all apply()s
      //  or do I? Just try with ALL
      val artifactory = project.plugins.findPlugin(ArtifactoryPlugin::class.java)
        ?: throw GradleException("unexpected: Artifactory plugin didn't apply")
    }
}
