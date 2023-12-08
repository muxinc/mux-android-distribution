package git.utils

@Suppress("unused")
object Git {

  /**
   * Returns the commit message of the head commit
   */
  fun headCommitMessage() = execGitRaw("log", listOf("-1", "--pretty=%B"))

  fun shortCommit() = execGitRaw("rev-parse", listOf("--short", "HEAD"))

  fun currentBranch() = execGitRaw("branch", listOf("--show-current"))

  fun describe() = execGitRaw("describe", listOf("--tags"))

  @Suppress("MemberVisibilityCanBePrivate")
  fun execGitRaw(gitCommand: String, commandArgs: List<String>?): String {
    val shellInput = if (commandArgs != null) {
      listOf("git", gitCommand) + commandArgs
    } else {
      listOf("git", gitCommand)
    }

    return Runtime.getRuntime().exec(shellInput.toTypedArray())
      .inputStream.bufferedReader().readLines().joinToString(separator = "\n").trim()
  }
}