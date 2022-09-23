package com.mux.gradle.android

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPom

abstract class MuxDistributionPluginExtension {

  protected Project project

  /**
   * If true, release via Artifactory
   * @return
   */
  abstract Property<Boolean> getUseArtifactory()

  abstract Property<String> getVersionFieldInBuildConfig()

  abstract Property<Boolean> getPackageJavadocs()

  abstract Property<Boolean> getPackageSources()

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

  abstract Property<Closure> getPublicReleaseIf()

  abstract Property<Closure> getPublishIf()

  protected Action<MavenPom> pomFunction

  protected ArtifactoryConfig artifactoryConfig = new ArtifactoryConfig()

  ArtifactoryConfig artifactoryConfig(Action<ArtifactoryConfig> action) {
    //noinspection GroovyAssignabilityCheck I do wish I could use kotlin
    artifactoryConfig = project.configure(artifactoryConfig, { action(it) })
    useArtifactory.set(true)
    return artifactoryConfig
  }

  void publishIf(Closure closure) {
    publishIf.set(closure)
  }

  void groupIds(Closure closure) {
    groupIdStrategy.set(closure)
  }

  void publicReleaseIf(Closure closure) {
    publicReleaseIf.set(closure)
  }

  void artifactIds(Closure closure) {
    artifactIdStrategy.set(closure)
  }

  void releaseVersion(Closure closure) {
    releaseVersionStrategy.set(closure)
  }

  void devVersion(Closure closure) {
    devVersionStrategy.set(closure)
  }

  void pom(Action<MavenPom> closure) {
    pomFunction = closure
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
    return { ['main', 'master'].contains(Git.currentBranch()) }
  }

  /**
   * Release all (non-filtered) variants if on the 'master' or 'main' branches, *and* the variant matches some predicate
   */
  def releaseOnMainBranchIf(Closure<Boolean> criteria) {
    return { releaseOnMainBranch().call() && criteria.call() }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def releaseOnTag() {
    // describe() returns a tag name on a tag(v1.2.3), or the distance to the nearest tag otherwise (v1.2.3-5-e4fc568a)
    return { (Git.describe() =~ /^v\d+\.\d+\.\d+$/).matches() && Git.currentBranch().isEmpty() }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def versionFromTag() {
    return { Git.describe() }
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
      def matcher = headCommit =~ /.*v(\d+\.\d+\.\d+)/
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
    return versionFromCommitHash("", Git.currentBranch())
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def versionFromCommitHash(String prefix) {
    return { prefix + "${Git.currentBranch()}-${Git.shortCommit()}" }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def versionFromCommitHash(String prefix, String branchName) {
    return { prefix + "$branchName-${Git.shortCommit()}" }
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
  def artifactFromAllFlavors() {
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
    return { variant -> buildTypes.findAll { variant.containsIgnoreCase(it) }.size() > 0 }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  def publishIfReleaseBuild() {
    return { variant -> variant.contains('Release') }
  }
}
