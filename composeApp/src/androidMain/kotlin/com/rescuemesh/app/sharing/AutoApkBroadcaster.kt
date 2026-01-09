package com.rescuemesh.app.sharing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

/**
 * AutoApkBroadcaster - Automatically sends APK to all discovered Bluetooth devices
 * 
 * When a room is created, this system:
 * 1. Starts Bluetooth discovery to find all nearby devices
 * 2. For each discovered device, attempts to send the APK via Bluetooth OPP
 * 3. The APK name serves as a message: "SOS-INSTALL-RESCUEMESH.apk"
 * 
 * The receiving device will see a notification like:
 * "Incoming file: SOS-INSTALL-RESCUEMESH.apk from [Device Name]"
 * 
 * This helps alert people that there's an emergency even if they don't accept the file.
 */
object AutoApkBroadcaster {
    
    private const val TAG = "AutoApkBroadcaster"
    
    // APK name that serves as a message
    private const val EMERGENCY_APK_NAME = "SOS-EMERGENCY-INSTALL-RESCUEMESH.apk"
    
    private val _isBroadcasting = MutableStateFlow(false)
    val isBroadcasting: StateFlow<Boolean> = _isBroadcasting.asStateFlow()
    
    private val _devicesFound = MutableStateFlow(0)
    val devicesFound: StateFlow<Int> = _devicesFound.asStateFlow()
    
    private val _sendAttempts = MutableStateFlow(0)
    val sendAttempts: StateFlow<Int> = _sendAttempts.asStateFlow()
    
    private val _statusLog = MutableStateFlow<List<String>>(emptyList())
    val statusLog: StateFlow<List<String>> = _statusLog.asStateFlow()
    
    private var discoveryReceiver: BroadcastReceiver? = null
    private var broadcastJob: Job? = null
    private val discoveredDevices = mutableSetOf<BluetoothDevice>()
    
    /**
     * Start automatic APK broadcasting to all nearby Bluetooth devices
     * Call this when creating a room
     */
    @SuppressLint("MissingPermission")
    fun startAutoBroadcast(context: Context, roomCode: String = "") {
        if (_isBroadcasting.value) {
            log("Already broadcasting...")
            return
        }
        
        _isBroadcasting.value = true
        _devicesFound.value = 0
        _sendAttempts.value = 0
        discoveredDevices.clear()
        
        log(" Starting emergency APK broadcast...")
        
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            log(" Bluetooth not available or disabled")
            _isBroadcasting.value = false
            return
        }
        
        // Check permissions
        if (!hasBluetoothPermissions(context)) {
            log(" Missing Bluetooth permissions")
            _isBroadcasting.value = false
            return
        }
        
        // Prepare the APK file with emergency name
        val apkUri = prepareEmergencyApk(context, roomCode)
        if (apkUri == null) {
            log(" Could not prepare APK file")
            _isBroadcasting.value = false
            return
        }
        
        log("OK: APK prepared: $EMERGENCY_APK_NAME")
        
        // Register for device discovery
        registerDiscoveryReceiver(context, apkUri)
        
        // Start discovery
        try {
            // First, make device discoverable
            requestDiscoverable(context)
            
            // Then start discovery
            if (bluetoothAdapter.startDiscovery()) {
                log("Signal: Scanning for nearby Bluetooth devices...")
            } else {
                log("WARNING: Could not start Bluetooth discovery")
            }
        } catch (e: SecurityException) {
            log(" Permission denied for Bluetooth discovery")
        }
        
        // Run periodic re-discovery
        broadcastJob = CoroutineScope(Dispatchers.Main).launch {
            repeat(5) { cycle ->
                delay(12_000) // Every 12 seconds
                if (_isBroadcasting.value) {
                    log(" Discovery cycle ${cycle + 2}/6...")
                    try {
                        bluetoothAdapter.cancelDiscovery()
                        delay(1000)
                        bluetoothAdapter.startDiscovery()
                    } catch (e: Exception) {
                        log("WARNING: Could not restart discovery: ${e.message}")
                    }
                }
            }
            
            // After 1 minute, stop
            stopAutoBroadcast(context)
            log("OK: Auto-broadcast completed. Found ${_devicesFound.value} devices, sent to ${_sendAttempts.value}")
        }
    }
    
    /**
     * Stop automatic broadcasting
     */
    @SuppressLint("MissingPermission")
    fun stopAutoBroadcast(context: Context) {
        broadcastJob?.cancel()
        broadcastJob = null
        
        try {
            discoveryReceiver?.let {
                context.unregisterReceiver(it)
            }
        } catch (e: Exception) {
            // Ignore
        }
        discoveryReceiver = null
        
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bluetoothManager?.adapter?.cancelDiscovery()
        } catch (e: Exception) {
            // Ignore
        }
        
        _isBroadcasting.value = false
        log("STOP Auto-broadcast stopped")
    }
    
    /**
     * Prepare the APK with emergency name
     */
    private fun prepareEmergencyApk(context: Context, roomCode: String): Uri? {
        return try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            
            // Create cache directory
            val shareDir = File(context.cacheDir, "emergency_share")
            shareDir.mkdirs()
            
            // Name includes room code if available
            val fileName = if (roomCode.isNotEmpty()) {
                "SOS-ROOM-$roomCode-RESCUEMESH.apk"
            } else {
                EMERGENCY_APK_NAME
            }
            
            val destFile = File(shareDir, fileName)
            
            // Copy APK
            sourceApk.inputStream().use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            // Get URI
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing APK", e)
            null
        }
    }
    
    /**
     * Register receiver for Bluetooth device discovery
     */
    @SuppressLint("MissingPermission")
    private fun registerDiscoveryReceiver(context: Context, apkUri: Uri) {
        discoveryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                        
                        device?.let { onDeviceFound(context, it, apkUri) }
                    }
                    
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        log("Signal: Discovery cycle finished. Found ${discoveredDevices.size} devices")
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(discoveryReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(discoveryReceiver, filter)
        }
    }
    
    /**
     * Called when a Bluetooth device is found
     */
    @SuppressLint("MissingPermission")
    private fun onDeviceFound(context: Context, device: BluetoothDevice, apkUri: Uri) {
        // Skip if already processed
        if (discoveredDevices.contains(device)) {
            return
        }
        
        discoveredDevices.add(device)
        _devicesFound.value = discoveredDevices.size
        
        val deviceName = try {
            device.name ?: device.address
        } catch (e: SecurityException) {
            device.address
        }
        
        log(" Found device: $deviceName")
        
        // Attempt to send APK via Bluetooth OPP
        sendApkToDevice(context, device, apkUri, deviceName)
    }
    
    /**
     * Send APK to a specific device via Bluetooth OPP
     */
    @SuppressLint("MissingPermission")
    private fun sendApkToDevice(context: Context, device: BluetoothDevice, apkUri: Uri, deviceName: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, apkUri)
                
                // Try to target specific device via Bluetooth
                setClassName(
                    "com.android.bluetooth",
                    "com.android.bluetooth.opp.BluetoothOppLauncherActivity"
                )
                
                // Add device address as extra (some systems support this)
                putExtra("android.bluetooth.device.extra.DEVICE", device)
                putExtra("destination", device.address)
                
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            _sendAttempts.value++
            log(" Sending APK to $deviceName...")
            
        } catch (e: Exception) {
            // Fallback: Try generic Bluetooth share
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    setPackage("com.android.bluetooth")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                context.startActivity(fallbackIntent)
                _sendAttempts.value++
                log(" Sending APK via Bluetooth (fallback)...")
                
            } catch (e2: Exception) {
                log("WARNING: Could not send to $deviceName: ${e2.message?.take(50)}")
            }
        }
    }
    
    /**
     * Request device to be discoverable
     */
    private fun requestDiscoverable(context: Context) {
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300) // 5 minutes
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(discoverableIntent)
        } catch (e: Exception) {
            log("WARNING: Could not request discoverable mode")
        }
    }
    
    /**
     * Check if we have required Bluetooth permissions
     */
    private fun hasBluetoothPermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Log a status message
     */
    private fun log(message: String) {
        Log.d(TAG, message)
        _statusLog.value = (_statusLog.value + message).takeLast(20)
    }
    
    /**
     * Get current status for display
     */
    fun getStatusMessage(isEnglish: Boolean): String {
        return if (_isBroadcasting.value) {
            if (isEnglish) {
                "Signal: Broadcasting APK... Found ${_devicesFound.value} devices, sent ${_sendAttempts.value} times"
            } else {
                "Signal: Transmitiendo APK... Encontrados ${_devicesFound.value} dispositivos, enviado ${_sendAttempts.value} veces"
            }
        } else {
            if (isEnglish) {
                "Auto-broadcast ready"
            } else {
                "Auto-transmisión lista"
            }
        }
    }
}
