/*
 */

tasks.register("assembleAll") {
  //dependsOn(gradle.includedBuild("distribution-old").task(":distribution-old:assemble"))
  val buildGitUtils = dependsOn(gradle.includedBuild("git-utils").task(":lib:assemble"))
  buildGitUtils.apply {
    dependsOn(gradle.includedBuild("mux-artifactory").task(":plugin:assemble"))
    dependsOn(gradle.includedBuild("dokka").task(":plugin:assemble"))
    dependsOn(gradle.includedBuild("android-publication").task(":plugin:assemble"))
  }
}
