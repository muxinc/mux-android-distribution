package com.mux.gradle.android

class DokkaPublishingConfig {
  String moduleName
  String footer
  boolean multiProject = false

  @Override
  public String toString() {
    return "DokkaPublishingConfig{" +
            "moduleName='" + moduleName + '\'' +
            ", footer='" + footer + '\'' +
            ", multiProject=" + multiProject +
            '}';
  }
}
