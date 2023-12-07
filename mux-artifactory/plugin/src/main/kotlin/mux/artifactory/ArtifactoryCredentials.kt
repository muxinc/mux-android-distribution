package mux.artifactory

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.*

data class ArtifactoryCredentials(
  val username: String,
  val password: String,
)

@Throws
fun artifactoryCredentialsFromLocalProperties(ofProject: Project): ArtifactoryCredentials {
  val username = fromEnvKey(USERNAME_KEY) ?: fromLocalPropertyKey(USERNAME_KEY, ofProject)
  val password = fromEnvKey(PASSWORD_KEY) ?: fromLocalPropertyKey(PASSWORD_KEY, ofProject)

  return if (username.isNullOrBlank() || password.isNullOrBlank()) {
    throw GradleException("Artifactory Username and Password are required if using muxArtifactory")
  }  else {
    ArtifactoryCredentials(username, password)
  }
}

private const val USERNAME_KEY = "artifactory_user"
private const val PASSWORD_KEY = "artifactory_password"

private fun fromEnvKey(key: String): String? {
  return System.getenv()["ORG_GRADLE_PROJECT_$key"]
}

private fun fromLocalPropertyKey(key: String, project: Project): String? {
  val propsFile = project.rootProject.file("local.properties")
  return if (propsFile.exists()) {
    val props = Properties().apply { load(propsFile.inputStream()) }
    props.getProperty(key)
  } else {
    null
  }
}