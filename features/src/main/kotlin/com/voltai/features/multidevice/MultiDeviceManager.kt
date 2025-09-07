package com.voltai.features.multidevice

import android.content.Context
import com.google.android.gms.wearable.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MultiDeviceManager @Inject constructor(
    private val context: Context
) : DataClient.OnDataChangedListener, MessageClient.OnMessageReceivedListener {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _deviceState = MutableStateFlow(DeviceState())
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()
    
    private val _connectedDevices = MutableStateFlow<List<ConnectedDevice>>(emptyList())
    val connectedDevices: StateFlow<List<ConnectedDevice>> = _connectedDevices.asStateFlow()
    
    private val dataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(context) }
    
    companion object {
        private const val BATTERY_DATA_PATH = "/battery_data"
        private const val DEVICE_INFO_PATH = "/device_info"
        private const val SYNC_REQUEST_PATH = "/sync_request"
        private const val CAPABILITY_WEAR_APP = "voltai_wear_app"
    }
    
    init {
        setupWearableListeners()
        discoverDevices()
    }
    
    private fun setupWearableListeners() {
        dataClient.addListener(this)
        messageClient.addListener(this)
    }
    
    private fun discoverDevices() {
        scope.launch {
            try {
                // Discover connected nodes (devices)
                val nodes = nodeClient.connectedNodes.await()
                val devices = mutableListOf<ConnectedDevice>()
                
                nodes.forEach { node ->
                    val device = ConnectedDevice(
                        id = node.id,
                        name = node.displayName,
                        type = if (node.isNearby) DeviceType.WEAR_OS else DeviceType.PHONE,
                        isConnected = true,
                        lastSeen = LocalDateTime.now(),
                        batteryInfo = null
                    )
                    devices.add(device)
                    
                    // Request battery info from discovered device
                    requestBatteryInfo(node.id)
                }
                
                _connectedDevices.value = devices
                _deviceState.value = _deviceState.value.copy(
                    connectedCount = devices.size,
                    isScanning = false
                )
                
            } catch (e: Exception) {
                _deviceState.value = _deviceState.value.copy(
                    isError = true,
                    errorMessage = "Failed to discover devices: ${e.message}"
                )
            }
        }
    }
    
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            when (event.type) {
                DataEvent.TYPE_CHANGED -> {
                    val dataItem = event.dataItem
                    when (dataItem.uri.path) {
                        BATTERY_DATA_PATH -> handleBatteryDataUpdate(dataItem)
                        DEVICE_INFO_PATH -> handleDeviceInfoUpdate(dataItem)
                    }
                }
                DataEvent.TYPE_DELETED -> {
                    // Handle deleted data if needed
                }
            }
        }
        dataEvents.release()
    }
    
    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            SYNC_REQUEST_PATH -> handleSyncRequest(messageEvent)
        }
    }
    
    private fun handleBatteryDataUpdate(dataItem: DataItem) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                val deviceId = dataMap.getString("device_id") ?: return@launch
                
                val batteryInfo = DeviceBatteryInfo(
                    batteryLevel = dataMap.getInt("battery_level"),
                    isCharging = dataMap.getBoolean("is_charging"),
                    temperature = dataMap.getFloat("temperature"),
                    voltage = dataMap.getFloat("voltage"),
                    health = dataMap.getInt("health"),
                    timestamp = dataMap.getLong("timestamp")
                )
                
                updateDeviceBatteryInfo(deviceId, batteryInfo)
                
            } catch (e: Exception) {
                _deviceState.value = _deviceState.value.copy(
                    isError = true,
                    errorMessage = "Failed to process battery data: ${e.message}"
                )
            }
        }
    }
    
    private fun handleDeviceInfoUpdate(dataItem: DataItem) {
        scope.launch {
            try {
                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                val deviceInfo = DeviceInfo(
                    id = dataMap.getString("device_id") ?: "",
                    name = dataMap.getString("device_name") ?: "",
                    model = dataMap.getString("model") ?: "",
                    manufacturer = dataMap.getString("manufacturer") ?: "",
                    osVersion = dataMap.getString("os_version") ?: "",
                    appVersion = dataMap.getString("app_version") ?: ""
                )
                
                updateDeviceInfo(deviceInfo)
                
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    private fun handleSyncRequest(messageEvent: MessageEvent) {
        scope.launch {
            // Respond to sync request with current device battery data
            sendBatteryDataToDevice(messageEvent.sourceNodeId)
        }
    }
    
    private suspend fun requestBatteryInfo(nodeId: String) {
        try {
            val message = "REQUEST_BATTERY_INFO".toByteArray()
            messageClient.sendMessage(nodeId, SYNC_REQUEST_PATH, message).await()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private suspend fun sendBatteryDataToDevice(nodeId: String) {
        try {
            val batteryData = getCurrentDeviceBatteryData()
            val dataMap = PutDataMapRequest.create(BATTERY_DATA_PATH).apply {
                dataMap.putString("device_id", android.os.Build.MODEL)
                dataMap.putInt("battery_level", batteryData.batteryLevel)
                dataMap.putBoolean("is_charging", batteryData.isCharging)
                dataMap.putFloat("temperature", batteryData.temperature)
                dataMap.putFloat("voltage", batteryData.voltage)
                dataMap.putInt("health", batteryData.health)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }
            
            val putDataRequest = dataMap.asPutDataRequest()
            dataClient.putDataItem(putDataRequest).await()
            
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    private fun updateDeviceBatteryInfo(deviceId: String, batteryInfo: DeviceBatteryInfo) {
        val devices = _connectedDevices.value.toMutableList()
        val deviceIndex = devices.indexOfFirst { it.id == deviceId }
        
        if (deviceIndex >= 0) {
            devices[deviceIndex] = devices[deviceIndex].copy(
                batteryInfo = batteryInfo,
                lastSeen = LocalDateTime.now()
            )
            _connectedDevices.value = devices
        } else {
            // Add new device
            val newDevice = ConnectedDevice(
                id = deviceId,
                name = "Unknown Device",
                type = DeviceType.UNKNOWN,
                isConnected = true,
                lastSeen = LocalDateTime.now(),
                batteryInfo = batteryInfo
            )
            devices.add(newDevice)
            _connectedDevices.value = devices
        }
    }
    
    private fun updateDeviceInfo(deviceInfo: DeviceInfo) {
        val devices = _connectedDevices.value.toMutableList()
        val deviceIndex = devices.indexOfFirst { it.id == deviceInfo.id }
        
        if (deviceIndex >= 0) {
            devices[deviceIndex] = devices[deviceIndex].copy(
                name = deviceInfo.name,
                type = determineDeviceType(deviceInfo)
            )
            _connectedDevices.value = devices
        }
    }
    
    private fun determineDeviceType(deviceInfo: DeviceInfo): DeviceType {
        return when {
            deviceInfo.model.contains("Watch", ignoreCase = true) -> DeviceType.WEAR_OS
            deviceInfo.manufacturer.equals("Samsung", ignoreCase = true) -> DeviceType.PHONE
            deviceInfo.manufacturer.equals("Google", ignoreCase = true) -> DeviceType.PHONE
            else -> DeviceType.UNKNOWN
        }
    }
    
    private fun getCurrentDeviceBatteryData(): DeviceBatteryInfo {
        val batteryStatus = context.registerReceiver(
            null,
            android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        )
        
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryLevel = (level * 100) / scale
        
        val status = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
        
        val temperature = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = (batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_VOLTAGE, 0) ?: 0) / 1000f
        val health = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_HEALTH, -1) ?: -1
        
        val healthPercentage = when (health) {
            android.os.BatteryManager.BATTERY_HEALTH_GOOD -> 100
            android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> 80
            android.os.BatteryManager.BATTERY_HEALTH_DEAD -> 0
            android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 70
            else -> 95
        }
        
        return DeviceBatteryInfo(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            temperature = temperature,
            voltage = voltage,
            health = healthPercentage,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun refreshDevices() {
        _deviceState.value = _deviceState.value.copy(isScanning = true)
        discoverDevices()
    }
    
    fun syncWithDevice(deviceId: String) {
        scope.launch {
            try {
                requestBatteryInfo(deviceId)
            } catch (e: Exception) {
                _deviceState.value = _deviceState.value.copy(
                    isError = true,
                    errorMessage = "Failed to sync with device: ${e.message}"
                )
            }
        }
    }
    
    fun disconnectDevice(deviceId: String) {
        val devices = _connectedDevices.value.toMutableList()
        devices.removeAll { it.id == deviceId }
        _connectedDevices.value = devices
        
        _deviceState.value = _deviceState.value.copy(
            connectedCount = devices.size
        )
    }
    
    fun getDeviceBatteryHistory(deviceId: String): List<BatteryHistoryEntry> {
        // In a real implementation, this would query the database
        return emptyList()
    }
    
    fun exportMultiDeviceReport(): String {
        val devices = _connectedDevices.value
        val report = StringBuilder()
        
        report.appendLine("=== VoltAI Multi-Device Battery Report ===")
        report.appendLine("Generated: ${LocalDateTime.now()}")
        report.appendLine("Connected Devices: ${devices.size}")
        report.appendLine()
        
        devices.forEach { device ->
            report.appendLine("Device: ${device.name} (${device.id})")
            report.appendLine("Type: ${device.type}")
            report.appendLine("Status: ${if (device.isConnected) "Connected" else "Disconnected"}")
            report.appendLine("Last Seen: ${device.lastSeen}")
            
            device.batteryInfo?.let { battery ->
                report.appendLine("Battery Level: ${battery.batteryLevel}%")
                report.appendLine("Charging: ${if (battery.isCharging) "Yes" else "No"}")
                report.appendLine("Temperature: ${battery.temperature}°C")
                report.appendLine("Voltage: ${battery.voltage}V")
                report.appendLine("Health: ${battery.health}%")
            }
            report.appendLine()
        }
        
        return report.toString()
    }
    
    fun cleanup() {
        dataClient.removeListener(this)
        messageClient.removeListener(this)
    }
}

// Data classes for multi-device monitoring
data class DeviceState(
    val isScanning: Boolean = false,
    val connectedCount: Int = 0,
    val lastSyncTime: LocalDateTime? = null,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class ConnectedDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val isConnected: Boolean,
    val lastSeen: LocalDateTime,
    val batteryInfo: DeviceBatteryInfo?
)

data class DeviceBatteryInfo(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val temperature: Float,
    val voltage: Float,
    val health: Int,
    val timestamp: Long
)

data class DeviceInfo(
    val id: String,
    val name: String,
    val model: String,
    val manufacturer: String,
    val osVersion: String,
    val appVersion: String
)

data class BatteryHistoryEntry(
    val timestamp: Long,
    val batteryLevel: Int,
    val isCharging: Boolean,
    val temperature: Float
)

enum class DeviceType {
    PHONE,
    WEAR_OS,
    TABLET,
    UNKNOWN
}
