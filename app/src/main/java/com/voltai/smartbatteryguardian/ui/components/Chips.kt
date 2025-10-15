package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FilterChipsRow(
    options: List<T>,
    selectedOption: T,
    onSelectedChange: (T) -> Unit,
    labelOf: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { opt ->
            FilterChip(
                selected = opt == selectedOption,
                onClick = { onSelectedChange(opt) },
                label = { Text(text = labelOf(opt)) },
            )
        }
    }
}
