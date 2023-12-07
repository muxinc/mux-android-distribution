package android.publication

import com.android.build.gradle.internal.api.BaseVariantImpl
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom

abstract class AndroidPublicationPluginExtension {
  internal lateinit var project: Project
  internal lateinit var plugin: AndroidPublicationPlugin

  internal var groupIdFn: ((variant: BaseVariantImpl) -> String)? = null
  internal var artifactIdFn: ((variant: BaseVariantImpl) -> String)? = null
  internal var releaseVersionFn: ((variant: BaseVariantImpl) -> String)? = null
  internal var devVersionFn: ((variant: BaseVariantImpl) -> String)? = null
  internal var publishIfFn: ((variant: BaseVariantImpl) -> Boolean)? = null
  internal var pomFn: ((variant: BaseVariantImpl) -> MavenPom)? = null

  abstract fun getPackageSources(): Property<Boolean>

  fun pom(pom: (variant: BaseVariantImpl) -> MavenPom) {
    pomFn = pom
  }

  fun groupId(block: (variant: BaseVariantImpl) -> String) {
    groupIdFn = block
  }

  fun artifactId(block: (variant: BaseVariantImpl) -> String) {
    artifactIdFn = block
  }
  fun releaseVersion(block: (variant: BaseVariantImpl) -> String) {
    releaseVersionFn = block
  }
  fun devVersion(block: (variant: BaseVariantImpl) -> String) {
    devVersionFn = block
  }
  fun publishIf(block: (variant: BaseVariantImpl) -> Boolean) {
    publishIfFn = block
  }
}