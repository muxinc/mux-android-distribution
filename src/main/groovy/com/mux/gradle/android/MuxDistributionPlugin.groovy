package com.mux.gradle.android

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.api.artifacts.PublishException
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.jfrog.gradle.plugin.artifactory.ArtifactoryPlugin

class MuxDistributionPlugin implements Plugin<Project> {

  private OkHttpClient httpClient = new OkHttpClient()
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
    initHttpClient()

    // We need depend on a couple of other plugins
    project.plugins.apply("maven-publish")
    project.plugins.apply(ArtifactoryPlugin.class)

    checkAndroidInstalled()
    initConventions()

    declareRepository()
    addVersionName()
    declarePublicationVariants()
    declarePublications()
    if (useArtifactory()) {
      configureArtifactory()
    }
  }

  @SuppressWarnings('GrUnresolvedAccess')
  private void declareRepository() {
    def artifactoryLogin = artifactoryCredentials()
    // TODO: In Android's new project setup, repositories are supposed to be in settings.gradle/a settings plugin
    //  To accommodate this, we'd need to declare another Plugin with the <Settings> type param, that can call
    //  settings.repositoryManagement {... }
    //  For now, users of this plugin can do repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS) to work around this
    project.repositories {
      maven {
        url "${extension.artifactoryContextUrl.get()}/${extension.artifactoryDevRepoKey.get()}"
        credentials {
          username = artifactoryLogin.username()
          password = artifactoryLogin.password()
        }
      }
    }
  }

  @SuppressWarnings('GrUnresolvedAccess')
  private void addVersionName() {
    // This doesn't add a "versionName" attribute to any android component/config, but it makes the calculated version
    //  available for stuff downstream
    project.version = deployVersion()

    // Android libraries have no "version name" field, but libs sometimes want the version available at runtime
    project.androidComponents {
      finalizeDsl() { ext ->
        ext.defaultConfig.buildConfigField 'String', extension.versionFieldInBuildConfig.get(), /"${deployVersion()}"/
      }
    }
  }

  @SuppressWarnings('GrUnresolvedAccess')
  private void declarePublicationVariants() {
    project.androidComponents {
      finalizeDsl() { ext ->
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

            variantNames.each {
              singleVariant(it) {
                if (extension.packageSources.get() == true) {
                  withSourcesJar()
                }
                if (extension.packageJavadocs.get() == true) {
                  withJavadocJar()
                }
              } // singleVariant(it)
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
        if (extension.publishIf.get().call(variant)) {
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
    } // afterEvaluate
  }

  @SuppressWarnings('GrUnresolvedAccess')
  private void configureArtifactory() {
    // Our projects' deployment strategy:
    //  Build with buildkite pipeline, distribute 'release' builds to 'local' repo
    //  For a "public release," do as above, then use the artifactory API to copy
    def artifactoryLogin = artifactoryCredentials()
    def ourContextUrl = extension.artifactoryContextUrl.get()
    def devRepoKey = extension.artifactoryDevRepoKey.get()
    project.artifactory {
      contextUrl = ourContextUrl
      publish {
        repository {
          repoKey = devRepoKey
          username = artifactoryLogin.username()
          password = artifactoryLogin.password()
        }
        defaults {
          publications createdPublications.toArray(*createdPublications)
        }
      }
    }

    project.tasks.create("muxReleaseDeploy") {
      doLast {
        project.android.libraryVariants.findAll {
          extension.publishIf.get().call(it)
        }.each {
          copyArtifactoryArtifacts(it)
        }
      }
    }
  }

  private void copyArtifactoryArtifacts(variant) {
    def devRepo = extension.artifactoryDevRepoKey.get()
    def releaseRepo = extension.artifactoryReleaseRepoKey.get()
    def base = "${extension.artifactoryContextUrl.get()}/api/copy"
    def repoPath = (extension.groupIdStrategy.get().call(variant) as String).replaceAll(/\./, '/')
    def name = extension.artifactIdStrategy.get().call(variant)
    def version = extension.releaseVersionStrategy.get().call(variant)

    // Assemble all that and POST to the artifactory API to copy from our dev repo to our public one
    def url = "$base/$devRepo/$repoPath/$name/$version?to=/$releaseRepo/$repoPath/$name/$version"
    def res = httpClient.newCall(new Request.Builder().url(url).post(RequestBody.create("".getBytes("UTF-8"))).build()).execute()
    if (!res.successful) {
      throw new PublishException("Couldn't publically release $name: HTTP ${res.code()} / ${res.message()}")
    }
  }

  private void initHttpClient() {
    def credentials = artifactoryCredentials()
    httpClient = new OkHttpClient.Builder()
            .authenticator(new BasicAuthenticator(credentials.username(), credentials.password()))
            .build()
  }

  private void initConventions() {
    extension.artifactIdStrategy.convention(extension.artifactFromFirstFlavor())
    extension.releaseVersionStrategy.convention(extension.versionFromHeadCommit())
    extension.devVersionStrategy.convention(extension.versionFromCommitHash("dev-"))
    extension.publicReleaseIf.convention(extension.releaseOnMainBranch())
    extension.groupIdStrategy.convention(extension.just("com.mux"))
    extension.publishIf.convention(extension.publishIfReleaseBuild())

    extension.useArtifactory.convention(true)
    extension.artifactoryDevRepoKey.convention('default-maven-local')
    extension.artifactoryReleaseRepoKey.convention("default-maven-release-local")
    extension.artifactoryContextUrl.convention("https://muxinc.jfrog.io/artifactory/")

    extension.versionFieldInBuildConfig.convention("LIB_VERSION")
    extension.packageJavadocs.convention(true)
    extension.packageSources.convention(true)
  }

  private ArtifactoryCredentials artifactoryCredentials() {
    // TODO: Based on some yet-to-make properties on the extension, override user/password
    return new SelfFetchArtifactoryCredentials(project)
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
