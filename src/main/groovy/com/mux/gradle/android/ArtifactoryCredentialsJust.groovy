package com.mux.gradle.android

class ArtifactoryCredentialsJust implements ArtifactoryCredentials {

  private String username;
  private String password;

  public ArtifactoryCredentialsJust(String username, String password) {
    this.username = username
    this.password = password
  }

  @Override
  String username() {
    return username
  }

  @Override
  String password() {
    return password
  }
}
