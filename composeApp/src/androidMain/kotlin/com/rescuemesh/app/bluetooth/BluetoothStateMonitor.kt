package com.rescuemesh.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitor de estado de Bluetooth para Android
 * Detecta cuando Bluetooth está desactivado y notifica a la UI
 */
class BluetoothStateMonitor(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }
    
    private val _isBluetoothEnabled = MutableStateFlow(checkBluetoothEnabled())
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()
    
    private val _bluetoothState = MutableStateFlow(getBluetoothState())
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState.asStateFlow()
    
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                updateState(state)
            }
        }
    }
    
    private var isReceiverRegistered = false
    
    /**
     * Inicia el monitoreo del estado de Bluetooth
     */
    fun startMonitoring() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(bluetoothReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(bluetoothReceiver, filter)
            }
            isReceiverRegistered = true
        }
        // Actualizar estado inicial
        _isBluetoothEnabled.value = checkBluetoothEnabled()
        _bluetoothState.value = getBluetoothState()
    }
    
    /**
     * Detiene el monitoreo
     */
    fun stopMonitoring() {
        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(bluetoothReceiver)
            } catch (e: Exception) {
                // Ignorar si no estaba registrado
            }
            isReceiverRegistered = false
        }
    }
    
    /**
     * Verifica si Bluetooth está habilitado
     */
    fun checkBluetoothEnabled(): Boolean {
        return try {
            bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            // Sin permiso, asumimos que no está disponible
            false
        }
    }
    
    /**
     * Obtiene el estado actual de Bluetooth
     */
    private fun getBluetoothState(): BluetoothState {
        if (bluetoothAdapter == null) {
            return BluetoothState.NOT_SUPPORTED
        }
        
        return try {
            when (bluetoothAdapter?.state) {
                BluetoothAdapter.STATE_ON -> BluetoothState.ENABLED
                BluetoothAdapter.STATE_OFF -> BluetoothState.DISABLED
                BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TURNING_ON
                BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TURNING_OFF
                else -> BluetoothState.UNKNOWN
            }
        } catch (e: SecurityException) {
            BluetoothState.PERMISSION_DENIED
        }
    }
    
    private fun updateState(adapterState: Int) {
        _isBluetoothEnabled.value = (adapterState == BluetoothAdapter.STATE_ON)
        _bluetoothState.value = when (adapterState) {
            BluetoothAdapter.STATE_ON -> BluetoothState.ENABLED
            BluetoothAdapter.STATE_OFF -> BluetoothState.DISABLED
            BluetoothAdapter.STATE_TURNING_ON -> BluetoothState.TURNING_ON
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothState.TURNING_OFF
            else -> BluetoothState.UNKNOWN
        }
    }
    
    /**
     * Verifica si el dispositivo soporta Bluetooth
     */
    fun isBluetoothSupported(): Boolean = bluetoothAdapter != null
}

/**
 * Estados posibles de Bluetooth
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
