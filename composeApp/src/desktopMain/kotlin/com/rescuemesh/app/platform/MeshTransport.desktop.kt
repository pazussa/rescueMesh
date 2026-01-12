package com.rescuemesh.app.platform

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.MeshPeer
import com.rescuemesh.app.model.IncidentRoom
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.*

/**
 * Información de una sala descubierta en la red
 */
data class DiscoveredRoom(
    val id: String,
    val name: String,
    val description: String,
    val peersCount: Int,
    val lastActivity: Long,
    val peers: Map<String, String> = emptyMap() // deviceId -> deviceName
)

/**
 * Desktop implementation of MeshTransport using UDP Multicast
 * This allows devices on the same local network to communicate without internet
 */
class DesktopMeshTransport : MeshTransport {
    
    companion object {
        private const val MULTICAST_GROUP = "239.255.42.99"
        private const val PORT = 45678
        private const val BUFFER_SIZE = 65536
        private const val ROOM_TIMEOUT_MS = 60000L // 60 seconds to consider room inactive
    }
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val json = Json { ignoreUnknownKeys = true }
    
    // State flows
    private val _isAdvertising = MutableStateFlow(false)
    private val _isDiscovering = MutableStateFlow(false)
    private val _connectedPeers = MutableStateFlow<Map<String, MeshPeer>>(emptyMap())
    private val _discoveredPeersCount = MutableStateFlow(0)
    private val _discoveredRooms = MutableStateFlow<Map<String, DiscoveredRoom>>(emptyMap())
    
    override val isAdvertising: StateFlow<Boolean> = _isAdvertising
    override val isDiscovering: StateFlow<Boolean> = _isDiscovering
    override val connectedPeers: StateFlow<Map<String, MeshPeer>> = _connectedPeers
    override val discoveredPeersCount: StateFlow<Int> = _discoveredPeersCount
    val discoveredRooms: StateFlow<Map<String, DiscoveredRoom>> = _discoveredRooms
    
    // Channels
    override val incomingMessages = Channel<MeshMessage>(Channel.BUFFERED)
    override val missingMessageRequests = Channel<Set<String>>(Channel.BUFFERED)
    
    // Callbacks
    override var getLocalInventory: (() -> Set<String>)? = null
    override var getMessageById: ((String) -> MeshMessage?)? = null
    
    // Network
    private var multicastSocket: MulticastSocket? = null
    private var multicastGroup: InetAddress? = null
    private var receiveJob: Job? = null
    private var heartbeatJob: Job? = null
    private var discoveryJob: Job? = null
    private var cleanupJob: Job? = null
    
    // Current session info
    private var currentDeviceId: String = ""
    private var currentDeviceName: String = ""
    private var currentRoom: IncidentRoom? = null
    
    // Discovery mode (before joining a room)
    private var discoveryMode = false
    
    // Seen messages (for deduplication)
    private val seenMessageIds = mutableSetOf<String>()
    
    /**
     * Start in discovery mode - listen for rooms without joining
     */
    fun startDiscovery(deviceId: String, deviceName: String) {
        currentDeviceId = deviceId
        currentDeviceName = deviceName
        discoveryMode = true
        
        try {
            multicastSocket = MulticastSocket(PORT).apply {
                reuseAddress = true
                timeToLive = 2
                loopbackMode = false
                soTimeout = 1000 // 1 second timeout for discovery
            }
            
            multicastGroup = InetAddress.getByName(MULTICAST_GROUP)
            
            val networkInterface = findSuitableInterface()
            if (networkInterface != null) {
                val socketAddress = InetSocketAddress(multicastGroup, PORT)
                multicastSocket?.joinGroup(socketAddress, networkInterface)
                println("[RescueMesh Desktop] Joined multicast on interface: ${networkInterface.displayName}")
            } else {
                multicastSocket?.joinGroup(multicastGroup)
            }
            
            _isDiscovering.value = true
            
            // Start listening for room announcements
            startDiscoveryLoop()
            
            // Start cleanup job
            startCleanupJob()
            
            println("[RescueMesh Desktop] Discovery mode started on $MULTICAST_GROUP:$PORT")
            
        } catch (e: Exception) {
            println("[RescueMesh Desktop] Error starting discovery: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun startDiscoveryLoop() {
        discoveryJob = scope.launch {
            val buffer = ByteArray(BUFFER_SIZE)
            val packet = DatagramPacket(buffer, buffer.size)
            
            while (isActive && discoveryMode) {
                try {
                    multicastSocket?.receive(packet)
                    val data = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    processDiscoveryData(data)
                } catch (e: SocketTimeoutException) {
                    // Normal timeout, continue
                } catch (e: SocketException) {
                    if (isActive && discoveryMode) {
                        println("[RescueMesh Desktop] Discovery socket error: ${e.message}")
                    }
                    break
                } catch (e: Exception) {
                    // Continue discovery
                }
            }
        }
    }
    
    private fun processDiscoveryData(data: String) {
        // Try heartbeat first (most common)
        try {
            val heartbeat = json.decodeFromString<HeartbeatPacket>(data)
            if (heartbeat.type == "heartbeat" && heartbeat.roomId.isNotEmpty()) {
                updateDiscoveredRoom(
                    roomId = heartbeat.roomId,
                    roomName = heartbeat.roomName,
                    roomDescription = heartbeat.roomDescription,
                    peerId = heartbeat.deviceId,
                    peerName = heartbeat.deviceName
                )
                return
            }
        } catch (e: Exception) {}
        
        // Try message
        try {
            val message = json.decodeFromString<MeshMessage>(data)
            if (message.roomId.isNotEmpty()) {
                updateDiscoveredRoom(
                    roomId = message.roomId,
                    roomName = "", // Unknown from message
                    roomDescription = "",
                    peerId = message.senderId,
                    peerName = message.senderName
                )
            }
        } catch (e: Exception) {}
    }
    
    private fun updateDiscoveredRoom(
        roomId: String,
        roomName: String,
        roomDescription: String,
        peerId: String,
        peerName: String
    ) {
        val current = _discoveredRooms.value.toMutableMap()
        val existing = current[roomId]
        
        val updatedPeers = (existing?.peers ?: emptyMap()).toMutableMap()
        updatedPeers[peerId] = peerName
        
        current[roomId] = DiscoveredRoom(
            id = roomId,
            name = if (roomName.isNotEmpty()) roomName else existing?.name ?: "Sala $roomId",
            description = if (roomDescription.isNotEmpty()) roomDescription else existing?.description ?: "",
            peersCount = updatedPeers.size,
            lastActivity = System.currentTimeMillis(),
            peers = updatedPeers
        )
        
        _discoveredRooms.value = current
    }
    
    private fun startCleanupJob() {
        cleanupJob = scope.launch {
            while (isActive) {
                delay(10000) // Every 10 seconds
                
                val cutoff = System.currentTimeMillis() - ROOM_TIMEOUT_MS
                val current = _discoveredRooms.value.toMutableMap()
                current.entries.removeAll { it.value.lastActivity < cutoff }
                _discoveredRooms.value = current
            }
        }
    }
    
    fun stopDiscovery() {
        discoveryMode = false
        discoveryJob?.cancel()
        cleanupJob?.cancel()
    }
    
    /**
     * Join a discovered room
     */
    fun joinRoom(deviceId: String, deviceName: String, roomId: String, roomName: String, roomDescription: String) {
        stopDiscovery()
        
        val room = IncidentRoom(
            id = roomId,
            name = roomName,
            description = roomDescription,
            creatorId = "unknown",
            pin = "", // No PIN needed for desktop
            createdAt = 0
        )
        
        startMesh(deviceId, deviceName, room)
    }
    
    override fun startMesh(deviceId: String, deviceName: String, room: IncidentRoom) {
        currentDeviceId = deviceId
        currentDeviceName = deviceName
        currentRoom = room
        discoveryMode = false
        
        try {
            // Only create socket if not already in discovery mode
            if (multicastSocket == null) {
                multicastSocket = MulticastSocket(PORT).apply {
                    reuseAddress = true
                    timeToLive = 2
                    loopbackMode = false
                }
                
                multicastGroup = InetAddress.getByName(MULTICAST_GROUP)
                
                val networkInterface = findSuitableInterface()
                if (networkInterface != null) {
                    val socketAddress = InetSocketAddress(multicastGroup, PORT)
                    multicastSocket?.joinGroup(socketAddress, networkInterface)
                } else {
                    multicastSocket?.joinGroup(multicastGroup)
                }
            }
            
            // Remove socket timeout for normal operation
            multicastSocket?.soTimeout = 0
            
            _isAdvertising.value = true
            _isDiscovering.value = true
            
            // Start receiving messages
            startReceiveLoop()
            
            // Start heartbeat for peer discovery
            startHeartbeat()
            
            println("[RescueMesh Desktop] Mesh started on $MULTICAST_GROUP:$PORT")
            println("[RescueMesh Desktop] Device: $deviceName ($deviceId)")
            println("[RescueMesh Desktop] Room: ${room.name} (${room.id})")
            
        } catch (e: Exception) {
            println("[RescueMesh Desktop] Error starting mesh: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun findSuitableInterface(): NetworkInterface? {
        return try {
            NetworkInterface.getNetworkInterfaces()?.toList()?.find { ni ->
                !ni.isLoopback && ni.isUp && ni.supportsMulticast() &&
                ni.inetAddresses.toList().any { it is Inet4Address }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun startReceiveLoop() {
        receiveJob = scope.launch {
            val buffer = ByteArray(BUFFER_SIZE)
            val packet = DatagramPacket(buffer, buffer.size)
            
            while (isActive && multicastSocket != null) {
                try {
                    multicastSocket?.receive(packet)
                    val data = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    processIncomingData(data, packet.address.hostAddress ?: "unknown")
                } catch (e: SocketException) {
                    if (isActive) {
                        println("[RescueMesh Desktop] Socket error: ${e.message}")
                    }
                    break
                } catch (e: Exception) {
                    println("[RescueMesh Desktop] Receive error: ${e.message}")
                }
            }
        }
    }
    
    private fun processIncomingData(data: String, sourceAddress: String) {
        try {
            // Try to parse as a message
            val message = json.decodeFromString<MeshMessage>(data)
            
            // Check if it's for our room and not seen before
            if (message.roomId == currentRoom?.id && message.id !in seenMessageIds) {
                seenMessageIds.add(message.id)
                
                scope.launch {
                    incomingMessages.send(message)
                }
                
                // Update peer count based on sender
                updatePeerSeen(message.senderId, message.senderName)
            }
        } catch (e: Exception) {
            // Try to parse as heartbeat
            try {
                val heartbeat = json.decodeFromString<HeartbeatPacket>(data)
                if (heartbeat.roomId == currentRoom?.id && heartbeat.deviceId != currentDeviceId) {
                    updatePeerSeen(heartbeat.deviceId, heartbeat.deviceName)
                }
            } catch (e2: Exception) {
                // Unknown packet format, ignore
            }
        }
    }
    
    private fun updatePeerSeen(peerId: String, peerName: String) {
        if (peerId != currentDeviceId) {
            val peer = MeshPeer(
                endpointId = peerId,
                deviceId = peerId,
                deviceName = peerName,
                roomId = currentRoom?.id ?: "",
                lastSeen = System.currentTimeMillis()
            )
            
            val updated = _connectedPeers.value.toMutableMap()
            updated[peerId] = peer
            
            // Clean old peers (not seen in 30 seconds)
            val cutoff = System.currentTimeMillis() - 30000
            updated.entries.removeAll { it.value.lastSeen < cutoff }
            
            _connectedPeers.value = updated
            _discoveredPeersCount.value = updated.size
        }
    }
    
    private fun startHeartbeat() {
        heartbeatJob = scope.launch {
            while (isActive) {
                try {
                    val heartbeat = HeartbeatPacket(
                        deviceId = currentDeviceId,
                        deviceName = currentDeviceName,
                        roomId = currentRoom?.id ?: "",
                        roomName = currentRoom?.name ?: "",
                        roomDescription = currentRoom?.description ?: "",
                        timestamp = System.currentTimeMillis()
                    )
                    
                    val data = json.encodeToString(heartbeat).toByteArray(Charsets.UTF_8)
                    val packet = DatagramPacket(
                        data,
                        data.size,
                        multicastGroup,
                        PORT
                    )
                    multicastSocket?.send(packet)
                    
                } catch (e: Exception) {
                    println("[RescueMesh Desktop] Heartbeat error: ${e.message}")
                }
                
                delay(5000) // Every 5 seconds
            }
        }
    }
    
    override fun stopMesh() {
        receiveJob?.cancel()
        heartbeatJob?.cancel()
        
        try {
            multicastGroup?.let { group ->
                val networkInterface = findSuitableInterface()
                if (networkInterface != null) {
                    val socketAddress = InetSocketAddress(group, PORT)
                    multicastSocket?.leaveGroup(socketAddress, networkInterface)
                } else {
                    multicastSocket?.leaveGroup(group)
                }
            }
            multicastSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        multicastSocket = null
        multicastGroup = null
        
        _isAdvertising.value = false
        _isDiscovering.value = false
        _connectedPeers.value = emptyMap()
        _discoveredPeersCount.value = 0
        seenMessageIds.clear()
        
        println("[RescueMesh Desktop] Mesh stopped")
    }
    
    override fun broadcastMessage(message: MeshMessage) {
        scope.launch {
            try {
                seenMessageIds.add(message.id)
                
                val data = json.encodeToString(message).toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    data,
                    data.size,
                    multicastGroup,
                    PORT
                )
                multicastSocket?.send(packet)
                
                println("[RescueMesh Desktop] Sent message: ${message.type} from ${message.senderName}")
                
            } catch (e: Exception) {
                println("[RescueMesh Desktop] Broadcast error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    override fun requestInventorySync() {
        // Request inventory from peers
        scope.launch {
            try {
                val request = InventorySyncRequest(
                    deviceId = currentDeviceId,
                    roomId = currentRoom?.id ?: "",
                    timestamp = System.currentTimeMillis()
                )
                
                val data = json.encodeToString(request).toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    data,
                    data.size,
                    multicastGroup,
                    PORT
                )
                multicastSocket?.send(packet)
                
            } catch (e: Exception) {
                println("[RescueMesh Desktop] Sync request error: ${e.message}")
            }
        }
    }
}

/**
 * Heartbeat packet for peer discovery - includes room info for discovery mode
 */
@kotlinx.serialization.Serializable
private data class HeartbeatPacket(
    val deviceId: String,
    val deviceName: String,
    val roomId: String,
    val roomName: String = "",
    val roomDescription: String = "",
    val timestamp: Long,
    val type: String = "heartbeat"
)

/**
 * Inventory sync request
 */
@kotlinx.serialization.Serializable
private data class InventorySyncRequest(
    val deviceId: String,
    val roomId: String,
    val timestamp: Long,
    val type: String = "inventory_request"
)

/**
 * Factory implementation for Desktop
 */
actual class MeshTransportFactory {
    actual fun create(): MeshTransport = DesktopMeshTransport()
}
