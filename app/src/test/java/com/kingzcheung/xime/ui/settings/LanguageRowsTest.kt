package com.kingzcheung.xime.ui.settings

import com.kingzcheung.xime.settings.InputLanguage
import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageRowsTest {
    private val all = InputLanguage.supported.map { it.id }

    @Test
    fun justTurnedOffLanguageAppearsOnce() {
        val staleOrder = listOf("zh", "en", "es", "de")
        val enabled = setOf(InputLanguage.CHINESE, InputLanguage.ENGLISH, InputLanguage.UNSPECIFIED)
        val rows = languageRows(staleOrder, enabled)
        assertEquals(rows.distinct(), rows)
        assertEquals(all.toSet(), rows.toSet())
        assertEquals(listOf("zh", "en"), rows.take(2))
    }

    @Test
    fun noMoreLanguagesRowWhenEveryLanguageIsOn() {
        val enabled = InputLanguage.supported.toSet()
        val rows = languageRows(InputLanguage.supported.map { it.id }, enabled)
        assertEquals(rows, languageListRows(rows, enabled, moreOpen = false))
    }

    @Test
    fun keepsDragOrderOfEnabledLanguages() {
        val enabled = setOf(InputLanguage.CHINESE, InputLanguage.ENGLISH)
        val rows = languageRows(listOf("en", "zh"), enabled)
        assertEquals(listOf("en", "zh"), rows.take(2))
    }
}
