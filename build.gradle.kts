/*
 */

tasks.register("assemble") {
  dependsOn(gradle.includedBuild("distribution").task(":distribution:assemble"))
  dependsOn(gradle.includedBuild("dokka").task(":dokka:assemble"))
}
