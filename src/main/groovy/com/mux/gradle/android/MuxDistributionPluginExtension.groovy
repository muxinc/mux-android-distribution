package com.mux.gradle.android

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.provider.Property

abstract class MuxDistributionPluginExtension {

  protected Project project

  abstract Property<Boolean> getUseArtifactory()

  abstract Property<String> getDeployRepoUrl()

  abstract Property<String> getArtifactoryRepoKey()

  abstract Property<String> getArtifactoryContextUrl()

  abstract Property<Closure> getGroupIdStrategy()

  /**
   * A Closure with a single parameter of the type LibraryVariant. Users may add logic to generate artifactIds for
   * each variant of their library
   */
  abstract Property<Closure> getArtifactIdStrategy()

  /**
   * A Closure with a single parameter of the type LibraryVariant. Users may add logic to generate the version of the
   * deployed library
   */
  abstract Property<Closure> getReleaseVersionStrategy()

  /**
   * A Closure with a single parameter of the type LibraryVariant. Users may add logic to generate the version of the
   * deployed library
   */
  abstract Property<Closure> getDevVersionStrategy()

  /**
   * A closure that takes a LibraryVariant and should return true if the variant should be built for release
   */
  abstract Property<Closure> getPublicReleaseIf()

  abstract Property<Closure> getPublishIf()

  public void publishIf(Closure closure) {
    publishIf.set(closure)
  }

  public void groupIds(Closure closure) {
    groupIdStrategy.set(closure)
  }

  public void publicReleaseIf(Closure closure) {
    publicReleaseIf.set(closure)
  }

  public void artifactIds(Closure closure) {
    artifactIdStrategy.set(closure)
  }

  public void releaseVersion(Closure closure) {
    releaseVersionStrategy.set(closure)
  }

  public void devVersion(Closure closure) {
    devVersionStrategy.set(closure)
  }

  // Pre-made config options

  @SuppressWarnings('GrMethodMayBeStatic')
  def just(String value) {
    return { value }
  }

  /**
   * Release all (non-filtered) variants if on the 'master' or 'main' branches. This the default behavior for picking
   * release builds
   */
  @SuppressWarnings('GrMethodMayBeStatic')
  def releaseOnMainBranch() {
    return { variant ->
      ['main', 'master'].contains(Git.currentBranch()) && !variant.buildType.isDebuggable()
    }
  }

  /**
   * Release all (non-filtered) variants if on the 'master' or 'main' branches, *and* the variant matches some predicate
   */
  def releaseOnMainBranchIf(Closure<Boolean> criteria) {
    return { variant -> releaseOnMainBranch().call(variant) && criteria.call(variant) }
  }

  /**
   * derives a version from a string in the commit message with the format "v1.2.3" with any number of digits per part
   */
  def versionFromHeadCommit() {
    return versionFromHeadCommit("")
  }

  /**
   * derives a version from a string in the commit message with the format "v1.2.3" with any number of digits per part.
   * you can also add a prefix, like 'v' or 'dev'
   *
   * This is the default for release builds
   */
  @SuppressWarnings('GrMethodMayBeStatic')
  def versionFromHeadCommit(String prefix) {
    return {
      def headCommit = Git.headCommitMessage()
      def matcher = headCommit =~ /v(\d+\.\d+\.\d+).*/
      if (matcher.matches()) {
        return prefix + matcher.group(1)
      } else {
        // If there's no vX.X.X then just use some characters from the message
        return prefix + headCommit.take(15)
      }
    }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def versionFromCommitHash() {
    return versionFromCommitHash("")
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def versionFromCommitHash(String prefix) {
    return { prefix + "${Git.currentBranch()}-${Git.shortCommit()}" }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def artifactFromProjectName() {
    return { variant -> project.name }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def artifactFromFirstFlavor() {
    return { variant ->
      if (variant.productFlavors.isEmpty()) {
        return project.name
      } else {
        def firstFlavor = variant.productFlavors.get(0)
        return "${project.name}_${firstFlavor.name}"
      }
    }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def artifactFromAllVariants() {
    return { variant ->
      def artifactId = project.name
      variant.productFlavors.each {
        artifactId += "_" + it.name
      }
      return artifactId
    }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def artifactFromFlavorValue(String flavorDimension) {
    return { variant ->
      def foundFlavors = variant.productFlavors.findAll { it.dimension == flavorDimension }.take(1)
      if (foundFlavors == null || foundFlavors.isEmpty()) {
        throw new ProjectConfigurationException("Couldn't generate artifactId, " +
                "no flavor dimension called \"$flavorDimension\"", null)
      } else {
        return "${project.name}_${foundFlavors.get(0).name}"
      }
    }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def publishAllOfBuildTypes(List<String> buildTypes) {
    return { variant -> buildTypes.contains(variant.buildType.name) }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def publishIfReleaseBuild() {
    return { variant -> !variant.buildType.isDebuggable() }
  }
}
