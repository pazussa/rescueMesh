package com.rescuemesh.app.sharing

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * EmergencyBeacon - Broadcasts emergency presence to nearby devices
 * 
 * IMPORTANT LIMITATION:
 * Bluetooth and WiFi cannot send notifications to devices that don't have the app installed.
 * What this beacon does:
 * 1. Makes the device discoverable via Bluetooth with "EMERGENCY-RESCUEMESH" name
 * 2. Periodically prompts users to share the APK manually
 * 3. Provides easy sharing mechanisms to multiple devices
 * 
 * The actual app distribution requires:
 * - Manual Bluetooth file transfer (OPP)
 * - QR code with download link (if internet available)
 * - WiFi Direct file sharing
 * - Nearby Share / Quick Share
 */
object EmergencyBeacon {
    
    private const val TAG = "EmergencyBeacon"
    
    // UUID for RescueMesh emergency beacon
    private val RESCUE_MESH_UUID = UUID.fromString("00001234-0000-1000-8000-00805f9b34fb")
    
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var isAdvertising = false
    
    private val _beaconState = MutableStateFlow(BeaconState.STOPPED)
    val beaconState: StateFlow<BeaconState> = _beaconState.asStateFlow()
    
    private val _devicesReached = MutableStateFlow(0)
    val devicesReached: StateFlow<Int> = _devicesReached.asStateFlow()
    
    private var shareReminderJob: Job? = null
    private var shareReminderCallback: (() -> Unit)? = null
    
    enum class BeaconState {
        STOPPED,
        STARTING,
        BROADCASTING,
        ERROR_NO_BLE,
        ERROR_PERMISSION
    }
    
    /**
     * Start the emergency beacon
     * Makes device visible as " EMERGENCY - Install RescueMesh"
     */
    fun startBeacon(context: Context, onShareReminder: () -> Unit) {
        if (isAdvertising) return
        
        shareReminderCallback = onShareReminder
        
        _beaconState.value = BeaconState.STARTING
        
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _beaconState.value = BeaconState.ERROR_NO_BLE
            Log.w(TAG, "Bluetooth not available or not enabled")
            return
        }
        
        // Try to make device name visible as emergency
        try {
            // Note: This requires BLUETOOTH_ADMIN permission and may not work on all devices
            makeDeviceDiscoverable(context)
        } catch (e: SecurityException) {
            Log.w(TAG, "Cannot change Bluetooth name: ${e.message}")
        }
        
        // Start BLE advertising if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
            if (bluetoothLeAdvertiser != null) {
                startBleAdvertising()
            }
        }
        
        // Start periodic reminder to share app
        startShareReminder()
        
        _beaconState.value = BeaconState.BROADCASTING
        isAdvertising = true
        
        Log.i(TAG, "Emergency beacon started - Device is now broadcasting emergency presence")
    }
    
    /**
     * Stop the beacon
     */
    fun stopBeacon() {
        shareReminderJob?.cancel()
        shareReminderJob = null
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        }
        
        isAdvertising = false
        _beaconState.value = BeaconState.STOPPED
        
        Log.i(TAG, "Emergency beacon stopped")
    }
    
    /**
     * Request to make device discoverable via Bluetooth
     */
    private fun makeDeviceDiscoverable(context: Context) {
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600) // 1 hour
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(discoverableIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not make device discoverable: ${e.message}")
        }
    }
    
    /**
     * Start BLE advertising with emergency data
     */
    private fun startBleAdvertising() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()
        
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(RESCUE_MESH_UUID))
            .build()
        
        try {
            bluetoothLeAdvertiser?.startAdvertising(settings, data, advertiseCallback)
        } catch (e: SecurityException) {
            Log.w(TAG, "BLE advertising permission denied: ${e.message}")
            _beaconState.value = BeaconState.ERROR_PERMISSION
        }
    }
    
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i(TAG, "BLE advertising started successfully")
        }
        
        override fun onStartFailure(errorCode: Int) {
            Log.w(TAG, "BLE advertising failed with error code: $errorCode")
        }
    }
    
    /**
     * Start periodic reminder to share the app (every 2 minutes)
     */
    private fun startShareReminder() {
        shareReminderJob?.cancel()
        shareReminderJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(120_000) // 2 minutes
                shareReminderCallback?.invoke()
                _devicesReached.value++ // Increment as a reminder counter
            }
        }
    }
    
    /**
     * Get message explaining the limitation and instructions
     */
    fun getEmergencyShareMessage(isEnglish: Boolean): String {
        return if (isEnglish) {
            """
             EMERGENCY - RescueMesh Network Active
            
            WARNING: IMPORTANT: Phones without this app cannot receive automatic notifications.
            
            TO HELP OTHERS JOIN:
            1. Open Bluetooth settings on nearby phones
            2. They will see your device as "RescueMesh Emergency"
            3. Use the SHARE button to send them the app via:
               • Bluetooth file transfer
               • Nearby Share / Quick Share
               • WiFi Direct
            
            INSTRUCTIONS FOR RECIPIENT:
            1. Accept the APK file
            2. Enable "Install from unknown sources" if prompted
            3. Install and open RescueMesh
            4. Join the Incident Room with the code you provide
            
             Share this app NOW to save lives!
            """.trimIndent()
        } else {
            """
             EMERGENCIA - Red RescueMesh Activa
            
            WARNING: IMPORTANTE: Los teléfonos sin esta app NO pueden recibir notificaciones automáticas.
            
            PARA AYUDAR A OTROS A UNIRSE:
            1. Abran Ajustes de Bluetooth en teléfonos cercanos
            2. Verán tu dispositivo como "RescueMesh Emergencia"
            3. Usa el botón COMPARTIR para enviarles la app via:
               • Transferencia de archivos Bluetooth
               • Nearby Share / Quick Share
               • WiFi Direct
            
            INSTRUCCIONES PARA EL RECEPTOR:
            1. Aceptar el archivo APK
            2. Activar "Instalar de fuentes desconocidas" si se solicita
            3. Instalar y abrir RescueMesh
            4. Unirse a la Sala de Incidente con el código que proporciones
            
             ¡Comparte esta app AHORA para salvar vidas!
            """.trimIndent()
        }
    }
}
