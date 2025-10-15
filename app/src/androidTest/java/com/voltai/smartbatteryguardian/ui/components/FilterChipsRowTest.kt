package com.voltai.smartbatteryguardian.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilterChipsRowTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun filterChipsRow_selectsAndExposesSelectionSemantics() {
        var selected by mutableStateOf(0)
        composeTestRule.setContent {
            FilterChipsRow(
                options = listOf(0, 1),
                selectedOption = selected,
                onSelectedChange = { selected = it },
                labelOf = { idx -> if (idx == 0) "Last 24h" else "Since unplug" },
            )
        }

        // Initially, "Last 24h" should be selected
        composeTestRule.onNode(
            isSelectable() and hasAnyDescendant(hasText("Last 24h")),
        ).assertIsSelected()

        // Click "Since unplug" and verify selection moves
        composeTestRule.onNodeWithText("Since unplug").performClick()
        composeTestRule.onNode(
            isSelectable() and hasAnyDescendant(hasText("Since unplug")),
        ).assertIsSelected()
    }
}
