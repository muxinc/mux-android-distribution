/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package android.publication

import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

/**
 * A simple functional test for the 'android.publication.greeting' plugin.
 */
class AndroidPublicationPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle") }

    @Test fun `can run task`() {
        // Set up the test build
        settingsFile.writeText("")
        buildFile.writeText("""
            plugins {
                id('android.publication.greeting')
            }
        """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("greeting")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("Hello from plugin 'android.publication.greeting'"))
    }
}
