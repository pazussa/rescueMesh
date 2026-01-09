package com.rescuemesh.app.platform

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.MeshPeer
import com.rescuemesh.app.model.IncidentRoom
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.channels.Channel

/**
 * Interface for mesh network transport layer
 * Implemented differently on Android (Nearby Connections) and iOS (MultipeerConnectivity)
 */
interface MeshTransport {
    // Connection state
    val isAdvertising: StateFlow<Boolean>
    val isDiscovering: StateFlow<Boolean>
    val connectedPeers: StateFlow<Map<String, MeshPeer>>
    val discoveredPeersCount: StateFlow<Int>
    
    // Incoming messages channel
    val incomingMessages: Channel<MeshMessage>
    
    // Missing message requests (for sync)
    val missingMessageRequests: Channel<Set<String>>
    
    // Callbacks
    var getLocalInventory: (() -> Set<String>)?
    var getMessageById: ((String) -> MeshMessage?)?
    
    // Operations
    fun startMesh(deviceId: String, deviceName: String, room: IncidentRoom)
    fun stopMesh()
    fun broadcastMessage(message: MeshMessage)
    fun requestInventorySync()
}

/**
 * Factory to create platform-specific MeshTransport
 */
expect class MeshTransportFactory {
    fun create(): MeshTransport
}
