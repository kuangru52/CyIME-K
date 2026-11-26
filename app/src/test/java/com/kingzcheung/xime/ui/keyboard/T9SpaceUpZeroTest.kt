package com.kingzcheung.xime.ui.keyboard

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kingzcheung.xime.settings.KeysConfigHelper
import com.kingzcheung.xime.settings.SettingsPreferences
import com.kingzcheung.xime.service.CandidateState
import com.kingzcheung.xime.viewmodel.KeyboardUiState
import com.kingzcheung.xime.viewmodel.KeyboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * 11-09：九键打英文等场景 0 键常接「输入法切换」（[com.kingzcheung.xime.settings.T9ZeroKeyAction.IME_SWITCH]）；
 * 打开「空格上滑打 0」时，按住空格往上滑打 0（非打字时；与 0 键去留解耦）。
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w800dp-h1280dp-xhdpi")
class T9SpaceUpZeroTest {
    @get:Rule val rule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    private val keys = mutableListOf<String>()
    private val moves = mutableListOf<Int>()
    private val candidate = mutableStateOf(CandidateState())

    private val controller = org.mockito.kotlin.mock<com.kingzcheung.xime.rime.T9InputController> {
        org.mockito.kotlin.whenever(it.leftPanelState).thenReturn(com.kingzcheung.xime.rime.T9InputController.LeftPanelState.IDLE)
    }

    private fun show(prefs: KeyboardInputPreferences) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        SettingsPreferences.getPrefsPublic(app).edit().clear().commit()
        prefs.save(app)
        KeysConfigHelper.loadConfig(app)
        KeysConfigHelper.setActiveKeyboardSchema("t9_pinyin")
        com.kingzcheung.xime.ui.theme.KeyboardThemes.initFromConfig(app)
        AppFonts.initialize(app)
        rule.setContent {
            CompositionLocalProvider(
                LocalKeyboardExplicitWidth provides true,
                LocalKeyboardInputPreferences provides prefs,
                LocalKeyboardInputActions provides KeyboardInputActions(onCursorMove = { moves += it })
            ) {
                MaterialTheme {
                    Box(Modifier.requiredSize(800.dp, 320.dp)) {
                        T9KeyboardLayout(
                            onKeyPress = { keys += it },
                            callbacks = KeyboardCallbacks(
                                onKeyPress = { key, _ -> keys += key },
                                onCandidateSelect = {},
                                onCursorMove = { moves += it }
                            ),
                            uiState = KeyboardUiState(currentSchemaId = "t9_pinyin"),
                            t9Controller = controller,
                            candidateState = candidate,
                            keyBackgroundColor = androidx.compose.ui.graphics.Color.LightGray,
                            keyTextColor = androidx.compose.ui.graphics.Color.Black,
                            specialKeyBackgroundColor = androidx.compose.ui.graphics.Color.Gray,
                            bubbleBackgroundColor = androidx.compose.ui.graphics.Color.DarkGray,
                            accentColor = androidx.compose.ui.graphics.Color.Blue,
                            keyboardBackgroundColor = androidx.compose.ui.graphics.Color.White,
                            shadowEnabled = false,
                            shadowElevation = 0.dp,
                            shadowShapeRadius = 8.dp,
                            keyCornerRadius = 8.dp,
                            modifier = Modifier.requiredSize(800.dp, 320.dp)
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    /** 在空格上往上滑（[sideways] 每步往旁边多走多少 dp）。 */
    private fun swipeSpaceUp(sideways: Float = 0f) {
        rule.onNodeWithTag("space-key", true).performTouchInput {
            val yStep = -12.dp.toPx()
            down(center)
            for (step in 1..8) moveTo(center + Offset(sideways.dp.toPx() * step, yStep * step))
            up()
        }
        rule.waitForIdle()
    }

    private fun tap(tag: String) {
        rule.onNodeWithTag(tag, true).performTouchInput { down(center); up() }
        rule.waitForIdle()
    }

    @Test fun offByDefaultSwipeUpDoesNothingAndZeroKeyTypesZero() {
        show(KeyboardInputPreferences(reverseSymbolSwipe = false))
        swipeSpaceUp()
        assertTrue("默认空格上滑什么都不做：$keys", keys.none { it == "0" || it == "space" })
        tap("t9-zero-key")
        assertTrue("默认 0 键点一下打 0：$keys", "0" in keys)
        assertTrue(moves.isEmpty())
    }

    @Test fun onSwipeUpTypesZeroAndTapIsStillSpace() {
        show(KeyboardInputPreferences(t9SpaceSwipeUpZero = true, reverseSymbolSwipe = false))
        swipeSpaceUp()
        assertEquals(listOf("ime_switch"), keys.filter { it == "ime_switch" || it == "space" })
        keys.clear()
        tap("space-key")
        assertTrue("点空格照旧是空格：$keys", "space" in keys && "ime_switch" !in keys)
    }

    @Test fun mostlySidewaysIsNotSwipeUp() {
        show(KeyboardInputPreferences(t9SpaceSwipeUpZero = true, reverseSymbolSwipe = false))
        swipeSpaceUp(sideways = 12f)
        assertTrue("往旁边多于往上不打 0：$keys", "0" !in keys)
    }

    @Test fun noZeroWhileTyping() {
        candidate.value = CandidateState(candidates = listOf("你好"), inputText = "64", preeditText = "ni", isComposing = true)
        show(KeyboardInputPreferences(t9SpaceSwipeUpZero = true, reverseSymbolSwipe = false))
        swipeSpaceUp()
        assertTrue("打字时空格上滑不打 0：$keys", "0" !in keys)
    }

    @Test fun zeroKeyIsArrowThatMovesCursorWhenIdle() {
        show(KeyboardInputPreferences(t9SpaceSwipeUpZero = true, reverseSymbolSwipe = false))
        tap("t9-zero-key")
        assertTrue("开了以后 0 键不再打 0：$keys", "0" !in keys)
        assertEquals(listOf(1), moves)
        rule.onNodeWithTag("t9-zero-key", true).performTouchInput {
            down(center)
            // 横滑门槛 50dp（SwipeableKeyButton）：拖 64dp 才算左滑，48dp 只是点按。
            for (step in 1..8) moveTo(center + Offset(-8.dp.toPx() * step, 0f))
            up()
        }
        rule.waitForIdle()
        assertEquals("左滑光标左移", listOf(1, -1), moves)
    }

    /** 去掉了 0 键时空格上滑一样切语言。 */
    @Test fun worksTogetherWithMergedZero() {
        show(KeyboardInputPreferences(t9SpaceSwipeUpZero = true, t9MergeZeroIntoEnter = true, reverseSymbolSwipe = false))
        swipeSpaceUp()
        assertTrue("$keys", "ime_switch" in keys)
    }
}
