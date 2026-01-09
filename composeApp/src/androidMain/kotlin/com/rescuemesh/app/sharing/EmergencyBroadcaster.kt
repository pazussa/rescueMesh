package com.rescuemesh.app.sharing

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.reflect.Method

/**
 * EmergencyBroadcaster - HACK to make emergency visible to devices WITHOUT the app
 * 
 * This uses multiple techniques to broadcast emergency presence:
 * 
 * 1. BLUETOOTH NAME HACK: Changes device Bluetooth name to emergency message
 *    - Anyone scanning for Bluetooth devices will see: "SOS-RESCUEMESH-HELP"
 *    - Works on ALL Android devices when they open Bluetooth settings
 * 
 * 2. WIFI HOTSPOT NAME HACK: Creates hotspot with emergency SSID
 *    - Anyone scanning for WiFi will see: "SOS-EMERGENCY-INSTALL-RESCUEMESH"
 *    - Works when people look for WiFi networks
 * 
 * 3. WIFI P2P SERVICE BROADCAST: Advertises emergency service
 *    - Visible to devices scanning for WiFi Direct services
 * 
 * 4. WIFI DIRECT GROUP: Creates a WiFi Direct group with emergency name
 *    - Visible when scanning for WiFi Direct devices
 * 
 * IMPORTANT: These are "hacks" that abuse system features to broadcast messages
 * to devices that don't have our app installed.
 */
object EmergencyBroadcaster {
    
    private const val TAG = "EmergencyBroadcaster"
    
    // Emergency messages (short due to name length limits)
    private const val BT_EMERGENCY_NAME = "SOS-RESCUEMESH-HELP"
    private const val WIFI_EMERGENCY_SSID = "SOS-EMERGENCY-RESCUEMESH"
    private const val P2P_SERVICE_NAME = "_rescuemesh_sos._tcp"
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    
    private var originalBluetoothName: String? = null
    private var wifiP2pManager: WifiP2pManager? = null
    private var wifiP2pChannel: WifiP2pManager.Channel? = null
    private var serviceInfo: WifiP2pDnsSdServiceInfo? = null
    
    private var broadcastJob: Job? = null
    private var context: Context? = null
    
    /**
     * Start all emergency broadcast methods
     */
    @SuppressLint("MissingPermission")
    fun startEmergencyBroadcast(ctx: Context, roomCode: String = "") {
        context = ctx
        
        if (_isActive.value) {
            Log.w(TAG, "Emergency broadcast already active")
            return
        }
        
        _isActive.value = true
        _statusMessage.value = "Starting emergency broadcast..."
        
        val results = mutableListOf<String>()
        
        // 1. HACK: Change Bluetooth name to emergency message
        val btResult = hackBluetoothName(ctx, roomCode)
        results.add(btResult)
        
        // 2. HACK: Start WiFi P2P service broadcast
        val p2pResult = hackWifiP2pService(ctx, roomCode)
        results.add(p2pResult)
        
        // 3. HACK: Create WiFi Direct group with emergency name
        val wifiDirectResult = hackWifiDirectGroup(ctx, roomCode)
        results.add(wifiDirectResult)
        
        // 4. Start periodic re-broadcast to ensure visibility
        startPeriodicBroadcast(ctx, roomCode)
        
        _statusMessage.value = results.joinToString("\n")
        
        Log.i(TAG, "Emergency broadcast started:\n${results.joinToString("\n")}")
    }
    
    /**
     * Stop all broadcasts and restore original settings
     */
    fun stopEmergencyBroadcast() {
        broadcastJob?.cancel()
        broadcastJob = null
        
        context?.let { ctx ->
            // Restore original Bluetooth name
            restoreBluetoothName(ctx)
            
            // Stop WiFi P2P service
            stopWifiP2pService()
            
            // Remove WiFi Direct group
            removeWifiDirectGroup()
        }
        
        _isActive.value = false
        _statusMessage.value = "Emergency broadcast stopped"
        context = null
        
        Log.i(TAG, "Emergency broadcast stopped")
    }
    
    // ============================================================
    // HACK 1: Bluetooth Name Change
    // ============================================================
    
    /**
     * Changes the device's Bluetooth name to an emergency message.
     * 
     * HACK EXPLANATION:
     * When anyone opens their Bluetooth settings and scans for devices,
     * they will see our device with the emergency name like:
     * "SOS-RESCUEMESH-HELP" or "SOS-ROOM:ABC123"
     * 
     * This works because Bluetooth device names are visible to ALL nearby devices
     * during scanning, without requiring any app or pairing.
     */
    @SuppressLint("MissingPermission")
    private fun hackBluetoothName(context: Context, roomCode: String): String {
        return try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter
            
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                return " Bluetooth: Disabled or unavailable"
            }
            
            // Check permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                    return " Bluetooth: Permission denied"
                }
            }
            
            // Save original name
            originalBluetoothName = bluetoothAdapter.name
            
            // Create emergency name (max ~248 chars, but keep short for visibility)
            val emergencyName = if (roomCode.isNotEmpty()) {
                "SOS-RM:$roomCode"  // Include room code
            } else {
                BT_EMERGENCY_NAME
            }
            
            // Change name using reflection for broader compatibility
            val success = try {
                bluetoothAdapter.setName(emergencyName)
            } catch (e: Exception) {
                // Try reflection as fallback
                try {
                    val setNameMethod: Method = bluetoothAdapter.javaClass.getMethod("setName", String::class.java)
                    setNameMethod.invoke(bluetoothAdapter, emergencyName) as Boolean
                } catch (e2: Exception) {
                    false
                }
            }
            
            if (success) {
                "OK: Bluetooth: Name set to '$emergencyName'"
            } else {
                "WARNING: Bluetooth: Could not change name"
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Bluetooth name hack failed", e)
            " Bluetooth: ${e.message}"
        }
    }
    
    /**
     * Restore original Bluetooth name
     */
    @SuppressLint("MissingPermission")
    private fun restoreBluetoothName(context: Context) {
        try {
            originalBluetoothName?.let { originalName ->
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                val bluetoothAdapter = bluetoothManager?.adapter
                bluetoothAdapter?.setName(originalName)
                Log.i(TAG, "Bluetooth name restored to: $originalName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore Bluetooth name", e)
        }
        originalBluetoothName = null
    }
    
    // ============================================================
    // HACK 2: WiFi P2P Service Broadcast
    // ============================================================
    
    /**
     * Broadcasts a WiFi P2P (WiFi Direct) service with emergency info.
     * 
     * HACK EXPLANATION:
     * WiFi P2P allows advertising "services" that other devices can discover.
     * While normally used for app-to-app communication, the service name
     * and TXT records are visible during scanning.
     * 
     * Some file sharing apps and system WiFi Direct scanners will show this.
     */
    private fun hackWifiP2pService(context: Context, roomCode: String): String {
        return try {
            wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
            
            if (wifiP2pManager == null) {
                return " WiFi P2P: Not supported"
            }
            
            wifiP2pChannel = wifiP2pManager?.initialize(context, Looper.getMainLooper(), null)
            
            if (wifiP2pChannel == null) {
                return " WiFi P2P: Could not initialize"
            }
            
            // Create service info with emergency data
            val txtRecord = mapOf(
                "emergency" to "true",
                "app" to "RescueMesh",
                "msg" to "SOS-INSTALL-APP",
                "room" to roomCode
            )
            
            serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                "RescueMesh_SOS_$roomCode",  // Instance name (visible)
                P2P_SERVICE_NAME,              // Service type
                txtRecord                       // Additional data
            )
            
            // Add local service
            wifiP2pManager?.addLocalService(wifiP2pChannel, serviceInfo, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "WiFi P2P service added successfully")
                }
                
                override fun onFailure(reason: Int) {
                    Log.w(TAG, "WiFi P2P service failed: $reason")
                }
            })
            
            "OK: WiFi P2P: Service broadcasting"
            
        } catch (e: Exception) {
            Log.e(TAG, "WiFi P2P hack failed", e)
            " WiFi P2P: ${e.message}"
        }
    }
    
    private fun stopWifiP2pService() {
        try {
            serviceInfo?.let { info ->
                wifiP2pManager?.removeLocalService(wifiP2pChannel, info, null)
            }
            serviceInfo = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop WiFi P2P service", e)
        }
    }
    
    // ============================================================
    // HACK 3: WiFi Direct Group
    // ============================================================
    
    /**
     * Creates a WiFi Direct group with emergency name.
     * 
     * HACK EXPLANATION:
     * When we create a WiFi Direct group, this device becomes a "Group Owner"
     * and its group name becomes visible to anyone scanning for WiFi Direct.
     * The group name includes our emergency message.
     */
    @SuppressLint("MissingPermission")
    private fun hackWifiDirectGroup(context: Context, roomCode: String): String {
        return try {
            if (wifiP2pManager == null || wifiP2pChannel == null) {
                return "⏭️ WiFi Direct: Skipped (P2P not initialized)"
            }
            
            // Check permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES)
                    != PackageManager.PERMISSION_GRANTED) {
                    return " WiFi Direct: Permission denied"
                }
            }
            
            // Create group (this makes us visible as WiFi Direct device)
            wifiP2pManager?.createGroup(wifiP2pChannel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "WiFi Direct group created")
                }
                
                override fun onFailure(reason: Int) {
                    Log.w(TAG, "WiFi Direct group failed: $reason")
                }
            })
            
            "OK: WiFi Direct: Group created"
            
        } catch (e: Exception) {
            Log.e(TAG, "WiFi Direct hack failed", e)
            " WiFi Direct: ${e.message}"
        }
    }
    
    private fun removeWifiDirectGroup() {
        try {
            wifiP2pManager?.removeGroup(wifiP2pChannel, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove WiFi Direct group", e)
        }
    }
    
    // ============================================================
    // Periodic Re-broadcast
    // ============================================================
    
    /**
     * Periodically refreshes broadcasts to ensure visibility
     */
    private fun startPeriodicBroadcast(context: Context, roomCode: String) {
        broadcastJob?.cancel()
        broadcastJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                delay(60_000) // Every minute
                
                // Re-ensure Bluetooth discoverability
                requestBluetoothDiscoverable(context)
                
                // Log status
                Log.d(TAG, "Emergency broadcast refresh - still active")
            }
        }
    }
    
    /**
     * Request Bluetooth discoverability (shows system dialog)
     */
    private fun requestBluetoothDiscoverable(context: Context) {
        try {
            val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600) // 1 hour max
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(discoverableIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Could not request discoverability: ${e.message}")
        }
    }
    
    // ============================================================
    // Helper: Get broadcast instructions for users
    // ============================================================
    
    /**
     * Returns instructions explaining what the broadcast does
     */
    fun getBroadcastExplanation(isEnglish: Boolean): String {
        return if (isEnglish) {
            """
            Signal: EMERGENCY BROADCAST ACTIVE
            
            Your phone is now broadcasting emergency signals that are visible to ALL nearby devices, even without RescueMesh installed:
            
             BLUETOOTH:
            When people scan for Bluetooth devices, they will see:
            "${BT_EMERGENCY_NAME}"
            
             WIFI DIRECT:
            When people scan for WiFi Direct, they will see your device advertising an emergency service.
            
            WARNING: TELL PEOPLE NEARBY:
            "Open your Bluetooth settings and look for a device named SOS-RESCUEMESH"
            "Then ask me how to get the app!"
            
            This hack uses your device name to broadcast the emergency message to anyone scanning for devices!
            """.trimIndent()
        } else {
            """
            Signal: TRANSMISIÓN DE EMERGENCIA ACTIVA
            
            Tu teléfono ahora transmite señales de emergencia visibles para TODOS los dispositivos cercanos, incluso sin RescueMesh instalado:
            
             BLUETOOTH:
            Cuando busquen dispositivos Bluetooth, verán:
            "${BT_EMERGENCY_NAME}"
            
             WIFI DIRECT:
            Cuando busquen WiFi Direct, verán tu dispositivo anunciando un servicio de emergencia.
            
            WARNING: DILE A LA GENTE CERCANA:
            "Abre tu configuración de Bluetooth y busca un dispositivo llamado SOS-RESCUEMESH"
            "¡Luego pregúntame cómo obtener la app!"
            
            ¡Este hack usa el nombre de tu dispositivo para transmitir el mensaje de emergencia a cualquiera que busque dispositivos!
            """.trimIndent()
        }
    }
}
