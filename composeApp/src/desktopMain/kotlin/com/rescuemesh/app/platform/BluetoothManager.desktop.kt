package com.rescuemesh.app.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Desktop implementation of BluetoothManager
 * Note: Desktop uses network UDP multicast instead of Bluetooth,
 * so this is a stub implementation that reports NOT_SUPPORTED
 */
class DesktopBluetoothManager : BluetoothManager {
    
    private val _isBluetoothEnabled = MutableStateFlow(false)
    private val _bluetoothState = MutableStateFlow(BluetoothState.NOT_SUPPORTED)
    
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled
    override val bluetoothState: StateFlow<BluetoothState> = _bluetoothState
    
    override fun startMonitoring() {
        // Desktop doesn't use Bluetooth - uses UDP multicast instead
        _bluetoothState.value = BluetoothState.NOT_SUPPORTED
        _isBluetoothEnabled.value = false
    }
    
    override fun stopMonitoring() {
        // Nothing to do
    }
    
    override fun checkBluetoothEnabled(): Boolean {
        // Desktop uses network, not Bluetooth
        return false
    }
    
    override fun isBluetoothSupported(): Boolean {
        // Desktop uses network communication instead
        return false
    }
    
    override fun openBluetoothSettings() {
        // No-op on desktop
        println("[RescueMesh Desktop] Bluetooth not used - Desktop uses network communication")
    }
}

/**
 * Factory implementation for Desktop
 */
actual class BluetoothManagerFactory {
    actual fun create(): BluetoothManager = DesktopBluetoothManager()
}
