# Mux Android Distribution Plugin

This is Mux's Gradle plugin for distributing our Android Libraries. It automatically creates publications for each build
variant that should be published, allowing them to be published to a repository of your choice. This plugin also
supports Artifactory

## Basic Usage

### Configure `buildscript`

In your project's top-level `build.gradle` file, add our repo and add the dependency

```groovy
buildscript {
  repositories {
    maven { url 'https://maven.pkg.github.com/muxinc/mux-android-distribution' }
  }
  dependencies {
    classpath 'com.mux.gradle.android:mux-android-distribution:0.1.0'
  }
}
```

### Apply, Configure & Go

In your Library Module's `build.gradle`, apply this plugin:

```groovy
plugins {
  id 'com.android.library'
  // other plugins
  id 'com.mux.gradle.android.mux-android-distribution'
}
```

This is a very basic config that will release all (non-debug) variants of your library, with the same group ID,
and `artifactId`s equal to the name and first product flavor.

```groovy
muxDistribution {
  groupIds just("com.your.organization.or.site")
}
```

After that's done, you can publish using your normal publishing tasks, either `publishVariantNameToRepo`
or `artifactoryPublish` or whatever strategy you use

```shell
$ ./gradlew publishAllPublicationsToGitHubPackagesRepository
# Or possibly
$ ./gradlew artifactoryPublish
```

## Advanced Usage

The plugin is very configurable. Artifact IDs, Group IDs, and Versions can be generated on a per-variant basis. There
are a few built-in strategies for generating each, and you can also write your own.

Here's the configuration we use for our ExoPlayer SDK:

```groovy
muxDistribution {
  devVersion versionFromCommitHash("dev-")
  releaseVersion versionFromHeadCommit()
  artifactIds artifactFromFlavorValue('api')
  groupIds just("com.mux.stats.sdk.muxstats")

  publishIf { !it.productFlavors*.name.contains("ads") && it.buildType.name.contains("release") }
  useArtifactory = true
}
```

### Pick Which Variants are Distributed

By default, all non-debug variants are distributed by this plugin. If you want the plugin to skip certain variants, you
can do so according to your own logic. Some prebuilt functions are provided

```groovy
muxDistribution {
  // publish all variants whose build type is one of "release" or "release-fat"
  publishIf = publishAllOfBuildTypes("release", "release-fat")
  // publish all non-debuggable variants
  publishIf = publishIfReleaseBuild()
  // publish all variants that have the flavor "companyWhiteLabel"
  publishIf { it.name.contains('companyWhiteLabel') }
}
```

### Configure Coordinates

#### Prebuilt Functions

There are prebuilt strategies creating maven coordinates. They cover some common cases, and you might find them useful.

| Coordinate                  | Strategy                                         | Description                                                                                                                                                       |
|-----------------------------|--------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| artifactId                  | `artifactFromProjectName`                        | generates an artifact ID from the name of the project/library module                                                                                              |
| artifactId                  | `artifactFromFirstFlavor`                        | generates an artifact ID from the name of the project/library module, then adding the value of the variant's first product flavor                                 |
| artifactId                  | `artifactFromAllFlavors`                         | generates an artifact ID from the name of the project/library module, then catenating the values of each flavor (in dimension order)                              |
| artifactId                  | `artifactFromFlavorValue`                        | generates an artifact ID from the name of the project/library module, then adding the value of the variant's product flavor with the given dimension name.        |
| devVersion, releaseVersion  | `versionFromHeadCommit`                          | generates a version name from the message of the HEAD commit of the current branch, looking for a token that looks like `v1.2.3` or something similarly-formatted |
| devVersion, releaseVersion  | `versionFromCommitHash(@Nullable String prefix)` | generates a version name from the hash of the current HEAD commit, and the name of the branch. You can supply a prefix, like `'dev;`                              |
| anything                    | `just(String)`                                   | Sets every variant's coordinate to the same supplied value. Can be used for any of: `groupIds`, `artifactIds`, `releaseVersion`, `devVersion`                     |

#### Using your own logic

You can also supply your own logic for generating coordinates. The object passed into these Closures has the same
properties as a `LibraryVariant`
For example,

```groovy
muxDistribution {
  // artifactId should be generated by prepending 'library_' to the name
  artifactIds { variant -> "library_${variant.name}" }
  // Release builds should all have the same version name: 1.0.1
  releaseVersion = just("1.0.1")
  // Dev builds should also include the CI build number if available
  devVersion { variant -> "1.0.1-dev-${System.getenv("BUILD_NUMBER")}" }
  // GroupId should be variant-sensitive
  groupIds { it.name.contains("whiteLabelA") ? "com.companyA.library.android" : "com.own.company.library.android" }
}
```
