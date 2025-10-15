package com.voltai.smartbatteryguardian.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.voltai.smartbatteryguardian.BatteryData
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomizableCardsGridSemanticsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun grid_exposesKeyA11yControls_whenInEditModeAndSelected() {
        val card = HomeCard(cardType = CardType.BATTERY_LEVEL, size = CardSize.Large, row = 0, column = 0)
        composeTestRule.setContent {
            CustomizableCardsGrid(
                cards = listOf(card),
                batteryData = BatteryData(),
                isEditMode = true,
                isDragActive = false,
                selectedCard = card,
                onCardClick = {},
                onCardLongPress = {},
                onCardRemove = {},
                onToggleVisibility = {},
                onCardMoveTo = { _, _ -> },
                onCardResize = { _, _ -> },
                onDragStart = {},
                onDragEnd = {},
                onAddCardClick = {},
                columns = 2,
            )
        }

        // Card exposes itself as a button with title
        composeTestRule.onNodeWithContentDescription("Battery Level card").assertIsDisplayed()

        // Edit controls
        composeTestRule.onNodeWithContentDescription("Hide Battery Level card").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Remove Battery Level card").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Resize Battery Level card").assertIsDisplayed()

        // Add card placeholder removed; actions live in top bar in edit mode
    }
}
