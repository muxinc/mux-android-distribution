package com.mux.gradle.git

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MuxVersionNamesTest {

  @Test
  fun `isDetachedOnReleaseTag happy path`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns ""
    every { Git.describe() } returns "v1.2.3"

    val detached = MuxVersionNames.isDetachedOnReleaseTag()
    assertTrue(detached, "If there's no branch name and a vX.Y.Z tag then we are 'on a release tag'")
  }

  @Test
  fun `isDetachedOnReleaseTag on a branch and NOT on a tag`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "xyzzy"
    every { Git.describe() } returns ""

    val detached = MuxVersionNames.isDetachedOnReleaseTag()
    assertFalse(detached, "If we're on a branch and there's no tag, we are not 'on a release tag'")
  }

  @Test
  fun `isDetachedOnReleaseTag on a branch on a tag`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "xyzzy"
    every { Git.describe() } returns "v1.2.3"

    val detached = MuxVersionNames.isDetachedOnReleaseTag()
    assertFalse(detached, "If we're on a branch and HEAD also has a tag, we are not 'on a release tag'")
  }

  @Test
  fun `versionNameFromCommit with a prefix`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "branch"
    every { Git.shortCommit() } returns "beebead"

    val versionName = MuxVersionNames.versionNameFromCommit("prefix-")
    val expectedVersionName = "prefix-branch-beebead"
    assertEquals(
      versionName,
      expectedVersionName,
      "Generated version name should be $expectedVersionName"
    )
  }

  @Test
  fun `versionNameFromCommit without a prefix`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "branch"
    every { Git.shortCommit() } returns "beebead"

    val versionName = MuxVersionNames.versionNameFromCommit()
    val expectedVersionName = "branch-beebead"
    assertEquals(
      versionName,
      expectedVersionName,
      "Generated version name should be $expectedVersionName"
    )
  }

  @Test
  fun `versionNameFromTag when NOT on a properly-formatted tag`() {
    mockkObject(Git)
    every { Git.describe() } returns "v1.2.3-g769fe52"

    val versionName = MuxVersionNames.versionNameFromTag()
    val expectedVersionName = "v1.2.3-g769fe52"
    assertEquals(
      versionName,
      expectedVersionName,
      "Generated version name should be $expectedVersionName"
    )
  }

  @Test
  fun `versionNameFromTag when on a properly-formatted tag`() {
    mockkObject(Git)
    every { Git.describe() } returns "v1.2.3"

    val versionName = MuxVersionNames.versionNameFromTag()
    val expectedVersionName = "1.2.3"
    assertEquals(
      versionName,
      expectedVersionName,
      "Generated version name should be $expectedVersionName"
    )
  }

  @Test
  fun `versionSafeBranchName should replace forward-slashes with dashes`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "testing/mock-branch-name with\\some-odd chars"

    val safeBranchName = MuxVersionNames.versionSafeBranchName()
    val expectedVersionName = "testing-mock-branch-name-with-some-odd-chars"
    assertEquals(
      safeBranchName,
      expectedVersionName,
      "Safe Branch names don't have slashes or spaces"
    )
  }
}