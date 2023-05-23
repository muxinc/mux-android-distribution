package com.mux.gradle.android

class DokkaPublishingConfig {
  String moduleName
  String footer

  @Override
  public String toString() {
    return "DokkaPublishingConfig{" +
            "moduleName='" + moduleName + '\'' +
            ", footer='" + footer + '\'' +
            '}';
  }
}
