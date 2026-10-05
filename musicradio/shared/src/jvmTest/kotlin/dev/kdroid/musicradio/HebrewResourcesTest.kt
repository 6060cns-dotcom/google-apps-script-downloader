package dev.kdroid.musicradio

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Android reports Hebrew as "iw" - the ISO 639 code retired in 1989 - and Compose Resources picks a
 * `values-*` bundle by string comparison, so `values-he` alone is unreachable there. The build
 * mirrors it into `values-iw`; see `mirrorHebrewStringsForAndroid` in shared/build.gradle.kts.
 *
 * This is here because the failure is silent. The mirror is wired to the Compose resource tasks by
 * name, and if an upgrade renames them the copy simply stops happening: the build stays green, the
 * APK ships without a Hebrew bundle it can reach, and the interface reverts to English on every
 * Android device. Nothing else in the project would notice.
 */
class HebrewResourcesTest {

    @Test
    fun `the Hebrew bundle is published under the code Android asks for`() {
        val resources = composeResources()
        val hebrew = File(resources, "values-he/strings.xml")
        val legacy = File(resources, "values-iw/strings.xml")
        assertTrue(hebrew.isFile, "values-he/strings.xml is missing from $resources")
        assertTrue(
            legacy.isFile,
            "values-iw/strings.xml was not generated. Android cannot reach values-he, so this " +
                "mirror is the only Hebrew bundle it ever loads - check that " +
                "mirrorHebrewStringsForAndroid still runs before the Compose resource tasks.",
        )
        assertEquals(
            hebrew.readText(),
            legacy.readText(),
            "values-iw has drifted from values-he. It is generated output: edit values-he.",
        )
    }

    /** Gradle runs tests from the module directory, but do not make the test depend on that. */
    private fun composeResources(): File {
        var dir: File? = File("").absoluteFile
        while (dir != null) {
            for (prefix in listOf("", "shared/")) {
                val candidate = File(dir, "${prefix}src/commonMain/composeResources")
                if (candidate.isDirectory) return candidate
            }
            dir = dir.parentFile
        }
        fail("No composeResources directory above ${File("").absolutePath}")
    }
}
