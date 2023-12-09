package com.mux.gradle.internal

import org.gradle.api.provider.Property

abstract class InternalDistPluginExtension {

  abstract fun getPluginVersion(): Property<String>
  abstract fun getArtifactId(): Property<String>
  abstract fun getGroupId(): Property<String>
}