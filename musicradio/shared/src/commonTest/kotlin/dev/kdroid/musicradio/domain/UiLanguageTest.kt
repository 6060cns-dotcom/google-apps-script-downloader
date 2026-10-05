package dev.kdroid.musicradio.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The OS hands the app whatever string its platform calls the current language, and Android still
 * uses the ISO 639 codes that were retired in 1989. Getting this wrong is invisible in a build:
 * the app simply falls back to English on every Hebrew device.
 */
class UiLanguageTest {

    @Test
    fun `the retired ISO codes still resolve`() {
        // What android.content.res.Configuration reports for a Hebrew device.
        assertEquals(UiLanguage.Hebrew, UiLanguage.fromCode("iw"))
        assertEquals(UiLanguage.Hebrew, UiLanguage.fromCode("iw-IL"))
    }

    @Test
    fun `the current codes resolve`() {
        assertEquals(UiLanguage.Hebrew, UiLanguage.fromCode("he"))
        assertEquals(UiLanguage.Hebrew, UiLanguage.fromCode("he_IL"))
        assertEquals(UiLanguage.French, UiLanguage.fromCode("fr-FR"))
        assertEquals(UiLanguage.English, UiLanguage.fromCode("EN"))
    }

    @Test
    fun `an unsupported language falls back to English`() {
        assertEquals(UiLanguage.English, UiLanguage.fromCode("ru"))
        assertEquals(UiLanguage.English, UiLanguage.fromCode(""))
    }

    @Test
    fun `every language is stored under its current code`() {
        // Round trip: whatever fromCode() accepts, the enum's own code has to map back to it.
        for (language in UiLanguage.entries) {
            assertEquals(language, UiLanguage.fromCode(language.code))
        }
    }
}
