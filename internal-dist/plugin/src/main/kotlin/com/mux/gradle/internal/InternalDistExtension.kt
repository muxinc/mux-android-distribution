package com.mux.gradle.internal

import org.gradle.api.provider.Property

abstract class InternalDistExtension {
  abstract fun getGroup(): Property<String>
  abstract fun getArtifactId(): Property<String>
}