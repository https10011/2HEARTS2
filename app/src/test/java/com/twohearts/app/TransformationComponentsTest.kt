package com.twohearts.app

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import com.twohearts.app.ui.components.ThButton
import com.twohearts.app.ui.components.ThInput
import com.twohearts.app.ui.components.ThModal
import com.twohearts.app.ui.components.ThSectionTabs
import com.twohearts.app.ui.theme.TwoHeartsTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w360dp-h780dp-mdpi")
class TransformationComponentsTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var activity: ActivityController<ComponentActivity>

    @Before
    fun createActivity() {
        activity = Robolectric.buildActivity(ComponentActivity::class.java).setup()
    }

    @After
    fun closeActivity() {
        if (::activity.isInitialized) activity.close()
    }

    private fun show(content: @Composable () -> Unit) {
        compose.runOnUiThread { activity.get().setContent(content = content) }
    }

    @Test
    fun pressFocusAndSelectionUseTheSharedControls() {
        val selected = mutableStateOf(0)
        var saves = 0
        show {
            TwoHeartsTheme(darkMode = false, reduceMotion = true) {
                Column {
                    ThSectionTabs(listOf("All notes", "Favorites"), selected.value, { selected.value = it })
                    ThInput(value = "", onValueChange = {}, label = "Title")
                    ThButton(onClick = { saves++ }, text = "Save note")
                    ThButton(onClick = { saves++ }, enabled = false, text = "Unavailable")
                    ThButton(onClick = { saves++ }, loading = true, text = "Saving")
                }
            }
        }

        compose.onNodeWithText("Favorites").performClick().assertIsSelected()
        compose.onNodeWithContentDescription("Title").performClick().assertIsFocused()
        compose.onNodeWithText("Unavailable").assertIsNotEnabled()
        compose.onNodeWithText("Saving").assertIsNotEnabled()
        compose.onNodeWithText("Save note").performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, saves) }
    }

    @Test
    fun sheetContentKeepsTheDialogOpenAndScrimDismissesIt() {
        val open = mutableStateOf(true)
        show {
            TwoHeartsTheme(darkMode = true) {
                ThModal(open = open.value, onClose = { open.value = false }, label = "Note editor") {
                    Text("Edit note")
                    ThInput(value = "Saved title", onValueChange = {}, label = "Title")
                }
            }
        }

        compose.onNodeWithContentDescription("Title").performClick()
        compose.runOnIdle { assertTrue("A tap on the field must keep the sheet open", open.value) }
        compose.onNodeWithContentDescription("Title").performSemanticsAction(SemanticsActions.RequestFocus).assertIsFocused()
        compose.onNodeWithText("Edit note").assertIsDisplayed()
        compose.onNode(isDialog()).performTouchInput { click(Offset(5f, 5f)) }
        compose.onNode(isDialog()).assertDoesNotExist()
    }
}
