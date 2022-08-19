package com.mux.gradle.android

import org.gradle.api.Project

class SelfFetchArtifactoryCredentials implements ArtifactoryCredentials {
  static final String USERNAME_KEY = 'artifactory_user'
  static final String PASSWORD_KEY = 'artifactory_password'

  private Project project;

  public SelfFetchArtifactoryCredentials(Project project) {
    this.project = project
  }

  @Override
  String username() {
    String envKeyValue = fromEnvKey(USERNAME_KEY)
    String propertyValue = fromLocalPropertyKey(USERNAME_KEY)
    return propertyValue != null ? propertyValue : envKeyValue
  }

  @Override
  String password() {
    String envKeyValue = fromEnvKey(PASSWORD_KEY)
    String propertyValue = fromLocalPropertyKey(PASSWORD_KEY)
    return propertyValue != null ? propertyValue : envKeyValue
  }

  @Override
  public String toString() {
    return "[username:${username()} , password:hidden]"
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  private String fromEnvKey(String key) {
    return System.getenv("ORG_GRADLE_PROJECT_$key")
  }

  private String fromLocalPropertyKey(String key) {
    def propsFile = project.rootProject.file('local.properties')
    if (propsFile.exists()) {
      Properties props = new Properties()
      props.load(propsFile.newDataInputStream())
      return props.getProperty(key)
    } else {
      return null
    }
  }
}
