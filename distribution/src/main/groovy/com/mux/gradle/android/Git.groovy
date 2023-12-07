package com.mux.gradle.android

class Git {
  private Git() {
    throw new IllegalAccessError("no instances of this class")
  }

  static String headCommitMessage() {
    return execGit("log", ["-1", "--pretty=%B"])
  }

  static String shortCommit() {
    return execGit("rev-parse", ["--short", "HEAD"])
  }

  static String currentBranch() {
    return execGit("branch", ["--show-current"])
  }

  static String describe() {
    return execGit("describe", ["--tags"])
  }

  private static String execGit(String gitCommand, List<String> argv) {
    def shellInput = ["git"] + gitCommand + (argv != null ? argv : [])
    return Runtime.getRuntime().exec(shellInput as String[]).inputStream.readLines().join("\n").trim()
  }
}
