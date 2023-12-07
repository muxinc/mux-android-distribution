package git.utils

import org.junit.jupiter.api.Test

class GitTest {

  @Test
  fun tryInvokingGit() {
    val output = Git.currentBranch()
  }
}