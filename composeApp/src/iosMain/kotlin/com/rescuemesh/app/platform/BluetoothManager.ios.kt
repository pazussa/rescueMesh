package com.rescuemesh.app.platform

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.CoreBluetooth.*
import platform.Foundation.*
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

/**
 * iOS implementation of BluetoothManager using CoreBluetooth
 */
class IOSBluetoothManager : BluetoothManager, NSObject(), CBCentralManagerDelegateProtocol {
    
    private var centralManager: CBCentralManager? = null
    
    private val _isBluetoothEnabled = MutableStateFlow(false)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled
    
    private val _bluetoothState = MutableStateFlow(BluetoothState.UNKNOWN)
    override val bluetoothState: StateFlow<BluetoothState> = _bluetoothState
    
    override fun startMonitoring() {
        if (centralManager == null) {
            centralManager = CBCentralManager(delegate = this, queue = null)
        }
    }
    
    override fun stopMonitoring() {
        centralManager = null
    }
    
    override fun checkBluetoothEnabled(): Boolean {
        return _isBluetoothEnabled.value
    }
    
    override fun isBluetoothSupported(): Boolean {
        // iOS devices generally support Bluetooth
        return true
    }
    
    override fun openBluetoothSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (url != null) {
            UIApplication.sharedApplication.openURL(url)
        }
    }
    
    // CBCentralManagerDelegate
    override fun centralManagerDidUpdateState(central: CBCentralManager) {
        val state = when (central.state) {
            CBManagerStatePoweredOn -> {
                _isBluetoothEnabled.value = true
                BluetoothState.ENABLED
            }
            CBManagerStatePoweredOff -> {
                _isBluetoothEnabled.value = false
                BluetoothState.DISABLED
            }
            CBManagerStateResetting -> {
                _isBluetoothEnabled.value = false
                BluetoothState.TURNING_OFF
            }
            CBManagerStateUnsupported -> {
                _isBluetoothEnabled.value = false
                BluetoothState.NOT_SUPPORTED
            }
            CBManagerStateUnauthorized -> {
                _isBluetoothEnabled.value = false
                BluetoothState.PERMISSION_DENIED
            }
            else -> {
                _isBluetoothEnabled.value = false
                BluetoothState.UNKNOWN
            }
        }
        _bluetoothState.value = state
    }
}

/**
 * Factory for iOS BluetoothManager
 */
actual class BluetoothManagerFactory {
    actual fun create(): BluetoothManager {
        return IOSBluetoothManager()
    }
}
