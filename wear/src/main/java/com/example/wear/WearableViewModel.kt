package com.voltai.wear

import androidx.lifecycle.ViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WearableViewModel : ViewModel(), DataClient.OnDataChangedListener {

    private val _batteryPercentage = MutableStateFlow(0)
    val batteryPercentage: StateFlow<Int> = _batteryPercentage.asStateFlow()

    private val _batteryStatus = MutableStateFlow("Unknown")
    val batteryStatus: StateFlow<String> = _batteryStatus.asStateFlow()

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == com.google.android.gms.wearable.DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/battery_status") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    _batteryPercentage.value = dataMap.getInt("battery_percentage")
                    _batteryStatus.value = dataMap.getString("battery_status") ?: "Unknown"
                }
            }
        }
    }
}
