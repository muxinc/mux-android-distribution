package com.mux.gradle.android.publication

import com.android.build.gradle.internal.api.BaseVariantImpl
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom

abstract class AndroidPublicationPluginExtension {
  internal lateinit var project: Project
  internal lateinit var plugin: AndroidPublicationPlugin

  internal var groupIdFn: ((variant: BaseVariantImpl) -> String)? = null
  internal var artifactIdFn: ((variant: BaseVariantImpl) -> String)? = null
  internal var releaseVersionFn: (() -> String)? = null
  internal var devVersionFn: (() -> String)? = null
  internal var publishVariantIfFn: ((variant: BaseVariantImpl) -> Boolean)? = null
  internal var publishToProdFn: (() -> Boolean)? = null
  internal var pomFn: (() -> MavenPom)? = null

  abstract fun getPackageSources(): Property<Boolean>

  fun pom(pom: () -> MavenPom) {
    pomFn = pom
  }

  fun groupId(block: (variant: BaseVariantImpl) -> String) {
    groupIdFn = block
  }

  fun artifactId(block: (variant: BaseVariantImpl) -> String) {
    artifactIdFn = block
  }
  fun releaseVersion(block: () -> String) {
    releaseVersionFn = block
  }
  fun devVersion(block: () -> String) {
    devVersionFn = block
  }
  fun publishVariantIf(block: (variant: BaseVariantImpl) -> Boolean) {
    publishVariantIfFn = block
  }
  fun publishToProdIf(block: () -> Boolean) {
    publishToProdFn = block
  }
}