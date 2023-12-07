package git.utils

import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class GitBasedVersionTest {

  @Test
  fun `isDetachedOnReleaseTag happy path`() {
    mockkObject(Git)
    every { Git.currentBranch() } returns ""
    every { Git.describe() } returns "v1.2.3"

    val detached = GitBasedVersion.isDetachedOnReleaseTag()
    assertTrue(detached, "If there's no branch name and a vX.Y.Z tag then we are on a relase tag")
  }
}