package com.voltai.smartbatteryguardian.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ElectricCurrentChartSemanticsTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun currentLineChart_exposesAccessibleDescription() {
        val samples = List(60) { it % 20 }
        composeTestRule.setContent {
            CurrentLineChart(samples = samples, window = CurrentWindow.W_1M, mode = CurrentMode.Discharging)
        }
        composeTestRule.onNodeWithContentDescription("Electric current chart").assertIsDisplayed()
    }
}
