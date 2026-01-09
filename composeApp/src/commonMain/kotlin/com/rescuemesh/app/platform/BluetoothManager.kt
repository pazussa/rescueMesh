package com.rescuemesh.app.platform

import kotlinx.coroutines.flow.StateFlow

/**
 * Bluetooth state enumeration
 */
enum class BluetoothState {
    ENABLED,
    DISABLED,
    TURNING_ON,
    TURNING_OFF,
    NOT_SUPPORTED,
    PERMISSION_DENIED,
    UNKNOWN
}

/**
 * Interface for Bluetooth state monitoring
 */
interface BluetoothManager {
    val isBluetoothEnabled: StateFlow<Boolean>
    val bluetoothState: StateFlow<BluetoothState>
    
    fun startMonitoring()
    fun stopMonitoring()
    fun checkBluetoothEnabled(): Boolean
    fun isBluetoothSupported(): Boolean
    fun openBluetoothSettings()
}

/**
 * Factory to create platform-specific BluetoothManager
 */
expect class BluetoothManagerFactory {
    fun create(): BluetoothManager
}
