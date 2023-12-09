package com.mux.gradle.internal

import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom

abstract class InternalDistExtension {
  internal var pomFn: ((MavenPom) -> Unit)? = null

  abstract fun getGroup(): Property<String>
  abstract fun getArtifactId(): Property<String>

  fun pom(pom: (MavenPom) -> Unit) {
    pomFn = pom
  }
}