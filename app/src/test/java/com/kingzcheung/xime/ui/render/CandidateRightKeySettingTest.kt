package com.kingzcheung.xime.ui.render

import android.content.Context
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingzcheung.xime.settings.SettingsPreferences
import com.kingzcheung.xime.ui.settings.LayoutDisplaySettingsContent
import com.kingzcheung.xime.ui.theme.KeyboardThemes
import com.kingzcheung.xime.ui.theme.XimeTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 候选栏两种用法（11-23 他：两套不能藏在「回弹」开关后面；11-24 定名「默认」「经典」）。
 * 「默认」= 1.15.101；选「经典」存成左右滑 + 关闭键放右边，「左右滑展开候选」锁成开着、取消按钮那两项不显示。
 * 存的键名不变（candidate_swipe_expand、candidate_close_on_right）。
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h760dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CandidateRightKeySettingTest {
    @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before @After fun reset() {
        SettingsPreferences.getPrefsPublic(context).edit()
            .remove(SettingsPreferences.KEY_CANDIDATE_SWIPE_EXPAND)
            .remove(SettingsPreferences.KEY_CANDIDATE_CLOSE_ON_RIGHT)
            .remove(SettingsPreferences.KEY_CANDIDATE_SWIPE_BEFORE_CLOSE)
            .remove(SettingsPreferences.KEY_SHOW_CANDIDATE_CANCEL_BUTTON).commit()
    }

    @Test fun defaultIsExpandAndCloseTurnsSwipeOn() {
        KeyboardThemes.initFromConfig(context)
        rule.setContent { XimeTheme(darkTheme = false, themeId = "soft_blue") {
            LayoutDisplaySettingsContent(onBack = {}, initialPage = "candidates") } }
        rule.waitForIdle()
        rule.onNode(hasScrollToNodeAction()).performScrollToNode(hasTestTag("candidate-right-key:close"))
        rule.onNodeWithTag("candidate-right-key:expand").assertIsSelected()
        rule.onNodeWithTag("candidate-right-key:close").assertIsNotSelected()
        rule.onNodeWithText("候选栏取消按钮").assertExists()

        rule.onNodeWithTag("candidate-right-key:close").performClick()
        rule.onNodeWithTag("candidate-right-key:close").assertIsSelected()
        assertEquals(true, SettingsPreferences.isCandidateSwipeExpand(context))
        assertEquals(true, SettingsPreferences.isCandidateCloseOnRight(context))
        rule.onNodeWithText("候选栏取消按钮").assertDoesNotExist()
        rule.onNodeWithText("选了「经典」时只能靠滑展开候选", substring = true).assertExists()

        // 选回「展开」：关闭键回左边，左右滑恢复成选「关闭」前的关着（11-23 平板实测）。
        rule.onNodeWithTag("candidate-right-key:expand").performClick()
        rule.onNodeWithTag("candidate-right-key:expand").assertIsSelected()
        assertEquals(false, SettingsPreferences.isCandidateCloseOnRight(context))
        assertEquals(false, SettingsPreferences.isCandidateSwipeExpand(context))
        rule.onNodeWithText("候选栏取消按钮").assertExists()
    }

    /** 选「关闭」前自己开着左右滑：选回「展开」后照旧开着；在「关闭」上再点一次也不会把记下的值盖掉。 */
    @Test fun swipeComesBackToWhatItWas() {
        SettingsPreferences.setCandidateSwipeExpand(context, true)
        SettingsPreferences.setCandidateRightKeyCloses(context, true)
        SettingsPreferences.setCandidateRightKeyCloses(context, false)
        assertEquals(true, SettingsPreferences.isCandidateSwipeExpand(context))

        SettingsPreferences.setCandidateSwipeExpand(context, false)
        SettingsPreferences.setCandidateRightKeyCloses(context, true)
        SettingsPreferences.setCandidateRightKeyCloses(context, true)
        assertEquals(true, SettingsPreferences.isCandidateSwipeExpand(context))
        SettingsPreferences.setCandidateRightKeyCloses(context, false)
        assertEquals(false, SettingsPreferences.isCandidateSwipeExpand(context))
        assertEquals(false, SettingsPreferences.isCandidateCloseOnRight(context))
    }
}
