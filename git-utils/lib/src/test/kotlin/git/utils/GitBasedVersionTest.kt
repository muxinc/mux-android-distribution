package git.utils

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GitBasedVersionTest {

  @Test
  fun `isDetachedOnReleaseTag happy path`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns ""
    every { Git.describe() } returns "v1.2.3"

    val detached = GitBasedVersion.isDetachedOnReleaseTag()
    assertTrue(detached, "If there's no branch name and a vX.Y.Z tag then we are 'on a release tag'")
  }

  @Test
  fun `isDetachedOnReleaseTag on a branch and NOT on a tag`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "xyzzy"
    every { Git.describe() } returns ""

    val detached = GitBasedVersion.isDetachedOnReleaseTag()
    assertFalse(detached, "If we're on a branch and there's no tag, we are not 'on a release tag'")
  }

  @Test
  fun `isDetachedOnReleaseTag on a branch on a tag`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns "xyzzy"
    every { Git.describe() } returns "v1.2.3"

    val detached = GitBasedVersion.isDetachedOnReleaseTag()
    assertFalse(detached, "If we're on a branch and HEAD also has a tag, we are not 'on a release tag'")
  }
}