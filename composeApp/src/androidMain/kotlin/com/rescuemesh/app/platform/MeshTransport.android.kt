package com.rescuemesh.app.platform

import android.content.Context
import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.MeshPeer
import com.rescuemesh.app.model.IncidentRoom
import com.rescuemesh.app.nearby.NearbyConnectionsManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.GlobalScope

/**
 * Android implementation of MeshTransport using Google Nearby Connections
 */
class AndroidMeshTransport(
    private val nearbyManager: NearbyConnectionsManager
) : MeshTransport {
    
    override val isAdvertising: StateFlow<Boolean> = nearbyManager.isAdvertising
    override val isDiscovering: StateFlow<Boolean> = nearbyManager.isDiscovering
    override val connectedPeers: StateFlow<Map<String, MeshPeer>> = nearbyManager.connectedPeers
    
    private val _discoveredPeersCount = MutableStateFlow(0)
    override val discoveredPeersCount: StateFlow<Int> = _discoveredPeersCount
    
    override val incomingMessages: Channel<MeshMessage> = nearbyManager.incomingMessages
    override val missingMessageRequests: Channel<Set<String>> = nearbyManager.missingMessageRequests
    
    override var getLocalInventory: (() -> Set<String>)?
        get() = nearbyManager.getLocalInventory
        set(value) { nearbyManager.getLocalInventory = value }
    
    override var getMessageById: ((String) -> MeshMessage?)?
        get() = nearbyManager.getMessageById
        set(value) { nearbyManager.getMessageById = value }
    
    init {
        // Update discovered peers count when it changes
        // This is a simplified approach; in production you'd use proper coroutine scope
    }
    
    override fun startMesh(deviceId: String, deviceName: String, room: IncidentRoom) {
        nearbyManager.startMesh(deviceId, deviceName, room)
    }
    
    override fun stopMesh() {
        nearbyManager.stopMesh()
    }
    
    override fun broadcastMessage(message: MeshMessage) {
        nearbyManager.broadcastMessage(message)
    }
    
    override fun requestInventorySync() {
        nearbyManager.requestInventorySync()
    }
}

/**
 * Factory for Android MeshTransport
 * Note: This requires Context, so it's initialized via AndroidPlatformContext
 */
actual class MeshTransportFactory {
    actual fun create(): MeshTransport {
        val context = AndroidPlatformContext.applicationContext
            ?: throw IllegalStateException("AndroidPlatformContext not initialized")
        return AndroidMeshTransport(NearbyConnectionsManager(context))
    }
}

/**
 * Holder for Android application context
 * Must be initialized in Application or MainActivity
 */
object AndroidPlatformContext {
    var applicationContext: Context? = null
    
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
}
