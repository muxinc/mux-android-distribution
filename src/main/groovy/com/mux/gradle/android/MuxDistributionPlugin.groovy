package com.mux.gradle.android

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jetbrains.dokka.gradle.DokkaPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin

class MuxDistributionPlugin implements Plugin<Project> {

  private Project project
  private MuxDistributionPluginExtension extension

  private List<Publication> createdPublications = new LinkedList<>()

  @SuppressWarnings('GrUnresolvedAccess')
  // Lots of stuff in the android dsl is invisible at compile time
  @Override
  void apply(Project project) {
    this.project = project
    extension = project.extensions.create("muxDistribution", MuxDistributionPluginExtension)
    extension.project = project
    extension.plugin = this

    // We need depend on a couple of other plugins
    project.plugins.apply("maven-publish")
    project.plugins.apply(ArtifactoryPlugin.class)

    checkAndroidInstalled()
    initConventions()

    processVariants()
    declarePublications()
  }

  @SuppressWarnings('GrUnresolvedAccess')
  void declareDokkaIfConfigured() {
    if (!extension.useDokka) {
      return
    } else {
      project.plugins.apply(DokkaPlugin.class)
    }

    DokkaPublishingConfig dokkaConfig = extension.dokkaPublishingConfig
    def moduleTitle = dokkaConfig.moduleName != null ? dokkaConfig.moduleName : project.name
    def footer = dokkaConfig.footer != null ? dokkaConfig.footer : ""
    configureDokkaTasks(project, moduleTitle, footer)
  }

  @SuppressWarnings('GrUnresolvedAccess')
  private void configureDokkaTasks(Project project, String title, String footer) {
    project.tasks.withType(DokkaTask.class) {
      dokkaSourceSets.configureEach {
        documentedVisibilities.set([Visibility.PUBLIC, Visibility.PROTECTED])
        moduleName.set(title)

        String dokkaBaseConfiguration = """
        {
          "customAssets": ["${project.file("logo-icon.svg")}"],
          "footerMessage": "$footer"
        }
        """
        pluginsMapConfiguration.set(["org.jetbrains.dokka.base.DokkaBase": dokkaBaseConfiguration])

        if (project.file("Module.md").exists()) {
          includes.from(project.file("Module.md"))
        }

        perPackageOption {
          matchingRegex.set(".*internal.*")
          suppress.set(true)
        }
      }
    }
  }

  @SuppressWarnings('GrUnresolvedAccess')
  private void processVariants() {
    project.androidComponents {
      finalizeDsl() { ext ->
        // This doesn't add a "versionName" attribute to any android component/config, but it makes the calculated version
        //  available for stuff downstream
        project.version = deployVersion()
        // Android libraries have no "version name" field, but libs sometimes want the version available at runtime
        ext.buildFeatures.buildConfig = true
        ext.defaultConfig.buildConfigField 'String', extension.versionFieldInBuildConfig.get(), /"${deployVersion()}"/
        project.logger.lifecycle("mux: Adding project version: '${project.version}'")

        // Declare Components for the libs we want to publish
        ext.publishing {
          def flavorContainer = new FlavorDimensionContainer()
          def productFlavors = ext.productFlavors
          def buildTypes = ext.buildTypes

          if (productFlavors != null && !productFlavors.isEmpty()) { // Declare publication variants for each variant
            // Variants aren't built yet but this is our last chance to declare publication variants, so build the names
            productFlavors.each {
              flavorContainer.addFlavor(it.dimension, it.name)
            }
            def variantNames = variantNames(flavorContainer.asMap().values() as List, buildTypes)

            variantNames.findAll { extension.publishIf.get().call(it) }.each {
              singleVariant(it) {
                if (extension.packageSources.get() == true) {
                  withSourcesJar()
                }
                if (extension.packageJavadocs.get() == true) {
                  withJavadocJar()
                }
              } // singleVariant(it)
              project.logger.info("mux: Created Component for: $it")
            } // variantNames.each {
          } else { // No flavors so just declare publication variants for by build types
            buildTypes*.name.each {
              singleVariant(it) {
                if (extension.packageSources.get() == true) {
                  withSourcesJar()
                }
                if (extension.packageJavadocs.get() == true) {
                  withJavadocJar()
                }
              } // singleVariant(it)
              project.logger.info("mux: Created Component for: $it")
            } // buildTypes*.names.each {
          } // else { // declare publication variants by build types
        }
      } // finalizeDsl
    } // project.androidComponents
  }

  private List<String> variantNames(List<Set<String>> flavorValues, Collection buildTypes) {
    def names = new ArrayList<String>()
    // First Dimension is different.
    flavorValues[0].each {
      names.addAll(variantNamesInner(flavorValues.subList(1, flavorValues.size()), it, buildTypes))
    }
    return names
  }

  private List<String> variantNamesInner(List<Set<String>> flavorValues, String nameSoFar, Collection buildTypes) {
    def names = new ArrayList<String>()
    if (flavorValues.isEmpty()) {
      // We got to the end. Return the name so far for each build type
      buildTypes.findAll { it != null }.each { buildType ->
        names.add(nameSoFar + Util.capitalizeFirstLetter(buildType.name))
      }
    } else {
      flavorValues[0].each { variantName ->
        def flavorName = Util.capitalizeFirstLetter(variantName)
        names.addAll(variantNamesInner(flavorValues.subList(1, flavorValues.size()), nameSoFar + flavorName, buildTypes))
      }
    }
    return names
  }

  @SuppressWarnings(['GrUnresolvedAccess', 'GroovyAssignabilityCheck'])
  private void declarePublications() {
    project.afterEvaluate {
      project.android.libraryVariants.each { variant ->
        if (extension.publishIf.get().call(variant.name)) {
          createdPublications += project.extensions.findByType(PublishingExtension).publications.create(variant.name, MavenPublication) { pub ->
            from project.components.findByName(variant.name)
            artifactId deployArtifactId(variant)
            version deployVersion()
            groupId extension.groupIdStrategy.get().call(variant)

            // configure the pom from outside
            if (extension.pomFunction != null) {
              pom { extension.pomFunction.execute(it) }
            }
          } // if(...)
        } // libraryVariants.each
      } // project.extensions...create()
      project.logger.info("mux: Created Publications: ${createdPublications*.name}")
    } // afterEvaluate
  }

  @SuppressWarnings('GrUnresolvedAccess')
  void configureArtifactory() {
    // Our projects' deployment strategy:
    //  Build with buildkite pipeline, distribute 'release' builds to 'local' repo
    //  For a "public release," do as above, then use the artifactory API to copy
    def artifactoryLogin = artifactoryCredentials()
    def ourContextUrl = extension.artifactoryConfig.contextUrl
    def devRepoKey = extension.artifactoryConfig.devRepoKey
    def releaseRepoKey = extension.artifactoryConfig.releaseRepoKey
    def publicRelease = extension.publicReleaseIf.get().call()
    project.artifactory {
      contextUrl = ourContextUrl
      publish {
        repository {
          repoKey = publicRelease ? releaseRepoKey : devRepoKey
          username = artifactoryLogin.username()
          password = artifactoryLogin.password()
        }
        defaults {
          publications createdPublications.toArray(*createdPublications)
        }
      }
    }
  }

  private void initConventions() {
    extension.artifactIdStrategy.convention(extension.artifactFromFirstFlavor())
    extension.releaseVersionStrategy.convention(extension.versionFromHeadCommit())
    extension.devVersionStrategy.convention(extension.versionFromCommitHash("dev-"))
    extension.publicReleaseIf.convention(extension.releaseOnMainBranch())
    extension.groupIdStrategy.convention(extension.just("com.mux"))
    extension.publishIf.convention(extension.publishIfReleaseBuild())

    extension.useArtifactory.convention(true)
    extension.artifactoryConfig.devRepoKey = 'NOT_CONFIGURED'
    extension.artifactoryConfig.releaseRepoKey = 'NOT_CONFIGURED'
    extension.artifactoryConfig.contextUrl = 'NOT_CONFIGURED'

    extension.versionFieldInBuildConfig.convention("LIB_VERSION")
    extension.packageJavadocs.convention(true)
    extension.packageSources.convention(true)
  }

  private ArtifactoryCredentials artifactoryCredentials() {
    if (extension.artifactoryConfig.username != null && extension.artifactoryConfig.password != null) {
      return new ArtifactoryCredentialsJust(extension.artifactoryConfig.username, extension.artifactoryConfig.password)
    } else {
      return new SelfFetchArtifactoryCredentials(project)
    }
  }

  private boolean useArtifactory() {
    return extension.useArtifactory && project.plugins.findPlugin(ArtifactoryPlugin.class) != null
  }

  private def deployArtifactId = { variant ->
    return extension.artifactIdStrategy.get().call(variant)
  }

  private def deployVersion = {
    if (extension.publicReleaseIf.get().call()) {
      return extension.releaseVersionStrategy.get().call().trim()
    } else {
      return extension.devVersionStrategy.get().call().trim()
    }
  }

  private void checkAndroidInstalled() {
    if (!project.plugins.hasPlugin('com.android.library')) {
      throw new ProjectConfigurationException('This plugin only works with android library modules', null)
    }
  }
}
