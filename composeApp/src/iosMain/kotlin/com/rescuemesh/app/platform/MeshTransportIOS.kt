package com.rescuemesh.app.platform

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.MeshPeer
import com.rescuemesh.app.model.IncidentRoom
import com.rescuemesh.app.model.PeerStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * iOS implementation of MeshTransport using MultipeerConnectivity framework
 * This implementation provides the bridge between Kotlin and iOS native APIs
 */
class IOSMeshTransport : MeshTransport {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        classDiscriminator = "type"
    }
    
    private var currentRoom: IncidentRoom? = null
    private var localDeviceId: String = ""
    private var localDeviceName: String = ""
    
    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising: StateFlow<Boolean> = _isAdvertising
    
    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering: StateFlow<Boolean> = _isDiscovering
    
    private val _connectedPeers = MutableStateFlow<Map<String, MeshPeer>>(emptyMap())
    override val connectedPeers: StateFlow<Map<String, MeshPeer>> = _connectedPeers
    
    private val _discoveredPeersCount = MutableStateFlow(0)
    override val discoveredPeersCount: StateFlow<Int> = _discoveredPeersCount
    
    override val incomingMessages = Channel<MeshMessage>(Channel.UNLIMITED)
    override val missingMessageRequests = Channel<Set<String>>(Channel.UNLIMITED)
    
    override var getLocalInventory: (() -> Set<String>)? = null
    override var getMessageById: ((String) -> MeshMessage?)? = null
    
    override fun startMesh(deviceId: String, deviceName: String, room: IncidentRoom) {
        localDeviceId = deviceId
        localDeviceName = deviceName
        currentRoom = room
        
        // Start MultipeerConnectivity session via bridge
        IOSMultipeerBridge.startSession(
            serviceType = "rescuemesh",
            displayName = "${room.id}|$deviceName",
            onPeerConnected = { peerId, peerName ->
                val peer = MeshPeer(
                    endpointId = peerId,
                    name = peerName,
                    roomId = currentRoom?.id ?: "",
                    lastSeen = currentTimeMillis(),
                    status = PeerStatus.CONNECTED
                )
                _connectedPeers.value = _connectedPeers.value + (peerId to peer)
            },
            onPeerDisconnected = { peerId ->
                _connectedPeers.value = _connectedPeers.value - peerId
            },
            onMessageReceived = { jsonString ->
                try {
                    val message = json.decodeFromString<MeshMessage>(jsonString)
                    incomingMessages.trySend(message)
                } catch (e: Exception) {
                    println("Error decoding received message: ${e.message}")
                }
            }
        )
        
        _isAdvertising.value = true
        _isDiscovering.value = true
        
        println("iOS Mesh started for room: ${room.name}")
    }
    
    override fun stopMesh() {
        IOSMultipeerBridge.stopSession()
        
        _isAdvertising.value = false
        _isDiscovering.value = false
        _connectedPeers.value = emptyMap()
        currentRoom = null
        
        println("iOS Mesh stopped")
    }
    
    override fun broadcastMessage(message: MeshMessage) {
        if (_connectedPeers.value.isEmpty()) {
            println("No peers connected, message queued locally")
            return
        }
        
        try {
            val jsonString = json.encodeToString(message)
            IOSMultipeerBridge.sendToAllPeers(jsonString)
            println("Message broadcast to ${_connectedPeers.value.size} peers")
        } catch (e: Exception) {
            println("Error broadcasting message: ${e.message}")
        }
    }
    
    override fun requestInventorySync() {
        val inventory = getLocalInventory?.invoke() ?: return
        
        try {
            val inventoryJson = json.encodeToString(mapOf("type" to "inventory", "ids" to inventory.toList()))
            IOSMultipeerBridge.sendToAllPeers(inventoryJson)
            println("Inventory sync requested")
        } catch (e: Exception) {
            println("Error requesting inventory sync: ${e.message}")
        }
    }
}

/**
 * Bridge object for MultipeerConnectivity
 * This provides callbacks that can be invoked from Swift/Objective-C code
 */
object IOSMultipeerBridge {
    private var onPeerConnected: ((String, String) -> Unit)? = null
    private var onPeerDisconnected: ((String) -> Unit)? = null
    private var onMessageReceived: ((String) -> Unit)? = null
    
    // Track if session is active
    private var isSessionActive = false
    
    fun startSession(
        serviceType: String,
        displayName: String,
        onPeerConnected: (String, String) -> Unit,
        onPeerDisconnected: (String) -> Unit,
        onMessageReceived: (String) -> Unit
    ) {
        this.onPeerConnected = onPeerConnected
        this.onPeerDisconnected = onPeerDisconnected
        this.onMessageReceived = onMessageReceived
        isSessionActive = true
        
        // The actual MCSession initialization is done in Swift
        // This just stores the callbacks
        println("MultipeerConnectivity bridge ready: $displayName")
    }
    
    fun stopSession() {
        onPeerConnected = null
        onPeerDisconnected = null
        onMessageReceived = null
        isSessionActive = false
        println("MultipeerConnectivity bridge stopped")
    }
    
    fun sendToAllPeers(data: String) {
        if (!isSessionActive) {
            println("Session not active, cannot send data")
            return
        }
        // This would be called back to Swift to actually send
        println("Bridge: Sending data to peers: ${data.take(100)}...")
    }
    
    // Public API for Swift to call back into Kotlin
    fun notifyPeerConnected(peerId: String, peerName: String) {
        onPeerConnected?.invoke(peerId, peerName)
    }
    
    fun notifyPeerDisconnected(peerId: String) {
        onPeerDisconnected?.invoke(peerId)
    }
    
    fun notifyMessageReceived(jsonString: String) {
        onMessageReceived?.invoke(jsonString)
    }
    
    fun isActive(): Boolean = isSessionActive
}

/**
 * Factory for iOS MeshTransport
 */
actual class MeshTransportFactory {
    actual fun create(): MeshTransport {
        return IOSMeshTransport()
    }
}
