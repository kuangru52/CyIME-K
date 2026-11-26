package com.kingzcheung.xime.settings

import org.junit.Assert.*
import org.junit.Test

class InputModesTest {
    private val modes = listOf("t9_pinyin", "rime_ice", "double_pinyin_flypy")
        .map { SchemaInfo(it, it, "", "", "") }

    @Test fun chineseModesIncludeShuangpinButNeverJapaneseOrEnglish() {
        assertEquals(listOf("t9_pinyin", "rime_ice", "double_pinyin_flypy"),
            InputModes.inCurrentLanguage(modes, "double_pinyin_flypy").map { it.schemaId })
    }
    @Test fun englishPanelsOnlyShowTheirOwnModes() {
        assertEquals(listOf(InputModes.ENGLISH),
            InputModes.inCurrentLanguage(modes, InputModes.ENGLISH).map { it.schemaId })
    }
    @Test fun languageMenuRestoresEachLanguageModeFromEnglish() {
        val choices = InputModes.languageChoices(modes, InputModes.ENGLISH,
            mapOf(InputLanguage.CHINESE to "double_pinyin_flypy"))
        assertEquals(listOf("中文", "英文"), choices.map { it.name })
        assertEquals(listOf("double_pinyin_flypy", InputModes.ENGLISH), choices.map { it.schemaId })
    }
    @Test fun addressFieldRemembersItsChineseLayoutWithoutChangingTheSavedProfile() {
        val saved = mapOf(InputLanguage.CHINESE to "rime_ice")
        val local = InputModes.languageChoices(modes, InputModes.ENGLISH, emptyMap(),
            selectedProfiles = saved, fieldNativeModeId = "t9_pinyin")
        assertEquals("t9_pinyin", local.first { it.language == InputLanguage.CHINESE }.schemaId)
        val ordinary = InputModes.languageChoices(modes, InputModes.ENGLISH, emptyMap(), selectedProfiles = saved)
        assertEquals("rime_ice", ordinary.first { it.language == InputLanguage.CHINESE }.schemaId)
    }
    @Test fun currentModeWinsAndRemovedModeFallsBackWithinItsLanguage() {
        val choices = InputModes.languageChoices(modes, "rime_ice",
            mapOf(InputLanguage.CHINESE to "t9_pinyin"))
        assertEquals(listOf("rime_ice", InputModes.ENGLISH), choices.map { it.schemaId })
    }
    @Test fun reorderingChineseModesPreservesEnglishSlots() {
        val current = listOf("t9_pinyin", "rime_ice", InputModes.ENGLISH)
        assertEquals(listOf("rime_ice", "t9_pinyin", InputModes.ENGLISH),
            InputModes.mergeOrder(current, listOf("rime_ice", "t9_pinyin")))
    }

    @Test fun independentLanguageOrderKeepsTheRememberedInputMode() {
        val order = InputModes.languageOrder(listOf("en", "unknown"))
        assertEquals(listOf(InputLanguage.ENGLISH, InputLanguage.CHINESE), order)
        val choices = InputModes.languageChoices(modes, "rime_ice", emptyMap(), order)
        assertEquals(listOf(InputModes.ENGLISH, "rime_ice"), choices.map { it.schemaId })
        assertEquals(listOf("rime_ice", "t9_pinyin"), InputModes.available(modes, InputModes.defaultModeOrder).take(2).map { it.schemaId })
        assertEquals("t9_pinyin", InputModes.available(modes, listOf("t9_pinyin", "rime_ice")).first().schemaId)
    }

    @Test fun englishIsAvailableWithNoEnabledSchemas() {
        assertEquals(listOf(InputModes.english), InputModes.available(emptyList()))
    }
    @Test fun customOrderSurvivesNormalizationAndNewSchemasAppend() {
        val chinese = SchemaInfo("t9", "拼音", "", "", "")
        val modes = InputModes.available(listOf(chinese, chinese), listOf(InputModes.ENGLISH, "t9"))
        assertEquals(listOf(InputModes.ENGLISH, "t9"), modes.map { it.schemaId })
        assertEquals(modes, InputModes.available(modes))
    }

    @Test fun everyLanguageListSharesOneOrder() {
        val zh = InputLanguage.CHINESE; val en = InputLanguage.ENGLISH
        assertEquals(listOf(zh, en), InputLanguage.supported)
        assertEquals(listOf(zh, en), InputLanguage.speech)
        val fresh = InputModes.languageRank(emptyList())
        assertEquals(listOf(zh, en, InputLanguage.UNSPECIFIED),
            listOf(InputLanguage.UNSPECIFIED, en, zh).sortedBy(fresh))
        assertEquals(listOf("中文", "英文"), InputModes.languageChoices(modes, "rime_ice", emptyMap()).map { it.name })
    }
}
