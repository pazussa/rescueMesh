package com.rescuemesh.app.platform

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.rescuemesh.app.bluetooth.BluetoothStateMonitor
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import com.rescuemesh.app.bluetooth.BluetoothState as AndroidBluetoothState

/**
 * Android implementation of BluetoothManager
 */
class AndroidBluetoothManager(
    private val context: Context
) : BluetoothManager {
    
    private val monitor = BluetoothStateMonitor(context)
    
    override val isBluetoothEnabled: StateFlow<Boolean> = monitor.isBluetoothEnabled
    
    // Map Android-specific state to common state
    private val _bluetoothState = MutableStateFlow(mapState(monitor.bluetoothState.value))
    override val bluetoothState: StateFlow<BluetoothState> = _bluetoothState
    
    private fun mapState(androidState: AndroidBluetoothState): BluetoothState {
        return when (androidState) {
            AndroidBluetoothState.ENABLED -> BluetoothState.ENABLED
            AndroidBluetoothState.DISABLED -> BluetoothState.DISABLED
            AndroidBluetoothState.TURNING_ON -> BluetoothState.TURNING_ON
            AndroidBluetoothState.TURNING_OFF -> BluetoothState.TURNING_OFF
            AndroidBluetoothState.NOT_SUPPORTED -> BluetoothState.NOT_SUPPORTED
            AndroidBluetoothState.PERMISSION_DENIED -> BluetoothState.PERMISSION_DENIED
            AndroidBluetoothState.UNKNOWN -> BluetoothState.UNKNOWN
        }
    }
    
    override fun startMonitoring() {
        monitor.startMonitoring()
    }
    
    override fun stopMonitoring() {
        monitor.stopMonitoring()
    }
    
    override fun checkBluetoothEnabled(): Boolean {
        return monitor.checkBluetoothEnabled()
    }
    
    override fun isBluetoothSupported(): Boolean {
        return monitor.isBluetoothSupported()
    }
    
    override fun openBluetoothSettings() {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

/**
 * Factory for Android BluetoothManager
 */
actual class BluetoothManagerFactory {
    actual fun create(): BluetoothManager {
        val context = AndroidPlatformContext.applicationContext
            ?: throw IllegalStateException("AndroidPlatformContext not initialized")
        return AndroidBluetoothManager(context)
    }
}
