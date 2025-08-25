package com.voltai.wear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WearableScreen(name: String, wearableViewModel: WearableViewModel = viewModel()) {
    val batteryPercentage by wearableViewModel.batteryPercentage.collectAsState()
    val batteryStatus by wearableViewModel.batteryStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimeText()
        Text(
            text = "VoltAI",
            style = MaterialTheme.typography.title1,
            color = MaterialTheme.colors.primary
        )
        Text(
            text = "Battery: $batteryPercentage%",
            style = MaterialTheme.typography.body1
        )
        Text(
            text = "Status: $batteryStatus",
            style = MaterialTheme.typography.body2
        )
    }
}
