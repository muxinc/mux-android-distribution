package com.mux.gradle.git

import java.util.regex.Pattern

/**
 * Utility functions that generate version strings from the output of git commands
 */
object MuxVersionNames {

  /**
   * The pattern used for matching the names of release tags
   */
  @Suppress("MemberVisibilityCanBePrivate")
  @JvmStatic
  val VERSION_TAG_NAME_PATTERN: Pattern = Pattern.compile("""^v(\d+\.\d+\.\d+)$""")

  /**
   * Returns true if the Git working tree is currently detached from a branch, but pointing
   * to a tag
   */
  @JvmStatic
  fun isDetachedOnReleaseTag(): Boolean {
    if (Git.currentBranch().isNotBlank()) {
      // not detached if there's a branch
      return false
    }

    val tagName = Git.describe()
    val matcher = VERSION_TAG_NAME_PATTERN.matcher(tagName).also { it.find() }
    return matcher.matches()
  }

  /**
   * Generates a version name from the shortened hash of the current commit and the name of the current branch
   */
  @JvmStatic
  fun versionNameFromCommit(prefix: String? = null): String {
    return if (prefix.isNullOrBlank()) {
      "${versionSafeBranchName()}-${Git.shortCommit()}"
    } else {
      "${prefix}${versionSafeBranchName()}-${Git.shortCommit()}"
    }
  }

  /**
   * Parses the version name out of a git tag
   */
  @JvmStatic
  fun versionNameFromTag(): String {
    val tagName = Git.describe()
    val matcher = VERSION_TAG_NAME_PATTERN.matcher(tagName).also { it.find() }
    return if (matcher.matches()) {
      matcher.group(1)
    } else {
      tagName
    }
  }

  /**
   * Returns a version of the branch name that is safe to use as a string readable by gradle
   */
  @JvmStatic
  fun versionSafeBranchName() = Git.currentBranch()
    .replace(Regex("""[/\\ ]"""), "-")
}