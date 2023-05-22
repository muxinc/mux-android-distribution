package com.mux.gradle.android

class DokkaPublishingConfig {
  String moduleName
  File logoFile
  String footer
  boolean multiProject = false

  @Override
  public String toString() {
    return "DocPublishingConfig{" +
            "moduleName='" + moduleName + '\'' +
            ", logoFile=" + logoFile +
            ", footer='" + footer + '\'' +
            '}';
  }
}
