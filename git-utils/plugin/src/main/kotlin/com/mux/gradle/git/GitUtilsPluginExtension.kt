package com.mux.gradle.git

/**
 * Provides some convenient methods for interacting with git to determine, eg
 * version names, deployment repos, etc
 */
abstract class GitUtilsPluginExtension {

  /**
   * Returns true only if not on a branch (ie, detached head) and *are* on a tag with
   * the format "vX.Y.Z"
   */
  fun isDetachedOnReleaseTag(): Boolean = GitBasedVersion.isDetachedOnReleaseTag()

  /**
   * Returns a version name based of the current commit, with an optional prefix. For example:
   * ```
   * // on branch 'topicbr' and commit `1234567'
   * versionNameFromCommit("dev-", "-beta") // returns dev-topicbr-1234567-beta
   * ```
   */
  fun versionNameFromCommit(prefix: String? = null, postfix: String? = null): String
    = GitBasedVersion.versionNameFromCommit(prefix, postfix)

  /**
   * Returns a version name based on the current tag. If the tag matches the format
   * [GitBasedVersion.VERSION_TAG_NAME_PATTERN]
   */
  fun versionNameFromTag(): String = GitBasedVersion.versionNameFromTag()

  /**
   * Returns the name of the current branch (if any), cleaned of characters that Gradle 8 doesn't like in
   * version strings
   */
  fun versionSafeBranchName(): String = GitBasedVersion.versionSafeBranchName()
}
