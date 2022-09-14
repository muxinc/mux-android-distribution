package com.mux.gradle.android

class ArtifactoryConfig {
  String username
  String password

  String contextUrl
  String devRepoKey
  String releaseRepoKey

  @Override
  String toString() {
    return "ArtifactoryConfig{" +
            "username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", contextUrl='" + contextUrl + '\'' +
            ", devRepoKey='" + devRepoKey + '\'' +
            ", releaseRepoKey='" + releaseRepoKey + '\'' +
            '}';
  }
}
