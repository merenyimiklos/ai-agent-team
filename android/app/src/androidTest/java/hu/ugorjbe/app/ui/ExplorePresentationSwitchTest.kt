package hu.ugorjbe.app.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import hu.ugorjbe.app.ui.theme.UgorjBeTheme
import hu.ugorjbe.app.ui.viewmodel.ExplorePresentation
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExplorePresentationSwitchTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun mapModeExposesListDestinationAndInvokesSwitch() {
        var clicks = 0
        composeRule.setContent {
            UgorjBeTheme {
                PresentationSwitch(ExplorePresentation.MAP) { clicks++ }
            }
        }

        composeRule.onNodeWithTag("explore_presentation_switch")
            .assertIsDisplayed()
            .assertTextContains("Lista")
            .performClick()

        composeRule.runOnIdle { assertEquals(1, clicks) }
    }
}
