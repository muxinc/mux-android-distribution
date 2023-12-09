package com.mux.gradle.git

import com.mux.gradle.git.Git
import org.junit.jupiter.api.Test

class GitTest {

  @Test
  fun tryInvokingGit() {
    val output = Git.currentBranch()
  }
}