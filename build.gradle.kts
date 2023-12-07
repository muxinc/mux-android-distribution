/*
 */

tasks.register("assemble") {
  dependsOn(gradle.includedBuild("distribution").task(":distribution:assemble"))
  dependsOn(gradle.includedBuild("dokka").task(":dokka:assemble"))
  dependsOn(gradle.includedBuild("distribution-old").task(":distribution-old:assemble"))
  dependsOn(gradle.includedBuild("git-utils").task(":git-utils:assemble"))
  dependsOn(gradle.includedBuild("mux-artifactory").task(":mux-artifactory:assemble"))
  dependsOn(gradle.includedBuild("android-publication").task(":android-publication:assemble"))
}
