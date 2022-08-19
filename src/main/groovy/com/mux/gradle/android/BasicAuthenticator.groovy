package com.mux.gradle.android

import okhttp3.*
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class BasicAuthenticator implements Authenticator {

  private String username
  private String password

  public BasicAuthenticator(String username, String password) {
    this.username = username
    this.password = password
  }

  @Override
  Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
    return response.request()
            .newBuilder()
            .addHeader("Authorization", Credentials.basic(username, password))
            .build()
  }
}
