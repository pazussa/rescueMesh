package com.rescuemesh.app.nearby

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.MeshPeer
import com.rescuemesh.app.model.IncidentRoom
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Protocolo de mensajes entre peers
 */
@Serializable
sealed class MeshProtocolMessage {
    /** Mensaje normal de la red mesh */
    @Serializable
    data class DataMessage(val message: MeshMessage) : MeshProtocolMessage()
    
    /** Inventario de mensajes que tenemos (para sincronización) */
    @Serializable
    data class Inventory(val messageIds: Set<String>) : MeshProtocolMessage()
    
    /** Solicitud de mensajes que nos faltan */
    @Serializable
    data class RequestMissing(val missingIds: Set<String>) : MeshProtocolMessage()
}

/**
 * Gestor de Nearby Connections para Android
 * Maneja descubrimiento, conexiones y envío/recepción de datos
 * Implementa el protocolo mesh con inventory/request-missing
 */
class NearbyConnectionsManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "NearbyConnectionsMgr"
        private const val SERVICE_ID = "com.rescuemesh.app"
        private val STRATEGY = Strategy.P2P_CLUSTER  // Permite múltiples conexiones
    }
    
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val json = Json { 
        ignoreUnknownKeys = true 
        classDiscriminator = "type"
    }
    
    // Estado de conexión
    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()
    
    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()
    
    // Peers conectados
    private val _connectedPeers = MutableStateFlow<Map<String, MeshPeer>>(emptyMap())
    val connectedPeers: StateFlow<Map<String, MeshPeer>> = _connectedPeers.asStateFlow()
    
    // Peers descubiertos (pendientes de conexión)
    private val _discoveredPeers = MutableStateFlow<Map<String, DiscoveredEndpointInfo>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, DiscoveredEndpointInfo>> = _discoveredPeers.asStateFlow()
    
    // Canal para mensajes recibidos
    val incomingMessages = Channel<MeshMessage>(Channel.UNLIMITED)
    
    // Canal para solicitudes de mensajes faltantes
    val missingMessageRequests = Channel<Set<String>>(Channel.UNLIMITED)
    
    // Callback para obtener inventario local
    var getLocalInventory: (() -> Set<String>)? = null
    
    // Callback para obtener mensaje por ID
    var getMessageById: ((String) -> MeshMessage?)? = null
    
    // Información del dispositivo local
    private var localDeviceId: String = ""
    private var localDeviceName: String = ""
    private var currentRoom: IncidentRoom? = null
    
    /**
     * Inicia advertising y discovery para una sala
     */
    fun startMesh(deviceId: String, deviceName: String, room: IncidentRoom) {
        localDeviceId = deviceId
        localDeviceName = deviceName
        currentRoom = room
        
        startAdvertising()
        startDiscovery()
    }
    
    /**
     * Detiene todo
     */
    fun stopMesh() {
        stopAdvertising()
        stopDiscovery()
        disconnectAll()
        currentRoom = null
    }
    
    /**
     * Inicia advertising (para que otros nos encuentren)
     */
    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        // El nombre incluye roomId para filtrar
        val endpointName = "${currentRoom?.id}|$localDeviceName"
        
        connectionsClient.startAdvertising(
            endpointName,
            SERVICE_ID,
            connectionLifecycleCallback,
            advertisingOptions
        ).addOnSuccessListener {
            _isAdvertising.value = true
            Log.d(TAG, "Advertising iniciado: $endpointName")
        }.addOnFailureListener { e ->
            _isAdvertising.value = false
            Log.e(TAG, "Error iniciando advertising: ${e.message}")
        }
    }
    
    /**
     * Detiene advertising
     */
    private fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        _isAdvertising.value = false
    }
    
    /**
     * Inicia discovery (para encontrar a otros)
     */
    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient.startDiscovery(
            SERVICE_ID,
            endpointDiscoveryCallback,
            discoveryOptions
        ).addOnSuccessListener {
            _isDiscovering.value = true
            Log.d(TAG, "Discovery iniciado para room: ${currentRoom?.id}")
        }.addOnFailureListener { e ->
            _isDiscovering.value = false
            Log.e(TAG, "Error iniciando discovery: ${e.message}")
        }
    }
    
    /**
     * Detiene discovery
     */
    private fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _isDiscovering.value = false
    }
    
    /**
     * Callback para discovery de endpoints
     */
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // Filtrar por roomId (está en el nombre del endpoint)
            val parts = info.endpointName.split("|")
            if (parts.size >= 2) {
                val roomId = parts[0]
                if (roomId == currentRoom?.id) {
                    // Es de nuestra sala, guardar y conectar
                    _discoveredPeers.value = _discoveredPeers.value + (endpointId to info)
                    requestConnection(endpointId)
                }
            }
        }
        
        override fun onEndpointLost(endpointId: String) {
            _discoveredPeers.value = _discoveredPeers.value - endpointId
        }
    }
    
    /**
     * Solicita conexión a un endpoint
     */
    private fun requestConnection(endpointId: String) {
        val localName = "${currentRoom?.id}|$localDeviceName"
        
        connectionsClient.requestConnection(
            localName,
            endpointId,
            connectionLifecycleCallback
        )
    }
    
    /**
     * Callback del ciclo de vida de conexiones
     */
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Auto-aceptar conexiones de la misma sala
            val parts = info.endpointName.split("|")
            if (parts.size >= 2 && parts[0] == currentRoom?.id) {
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            } else {
                connectionsClient.rejectConnection(endpointId)
            }
        }
        
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                // Conexión exitosa
                val discoveredInfo = _discoveredPeers.value[endpointId]
                val peerName = discoveredInfo?.endpointName?.split("|")?.getOrNull(1) ?: "Unknown"
                
                val peer = MeshPeer(
                    endpointId = endpointId,
                    deviceId = endpointId, // Usamos endpointId como deviceId
                    deviceName = peerName,
                    roomId = currentRoom?.id ?: "",
                    lastSeen = System.currentTimeMillis()
                )
                
                _connectedPeers.value = _connectedPeers.value + (endpointId to peer)
                _discoveredPeers.value = _discoveredPeers.value - endpointId
                
                // Al conectar, enviar nuestro inventario para sincronización
                sendInventoryTo(endpointId)
                
                Log.d(TAG, "Conectado a peer: $peerName ($endpointId)")
            } else {
                Log.w(TAG, "Conexión fallida con $endpointId: ${result.status}")
                // Remover de descubiertos para permitir re-descubrimiento
                _discoveredPeers.value = _discoveredPeers.value - endpointId
            }
        }
        
        override fun onDisconnected(endpointId: String) {
            val peerName = _connectedPeers.value[endpointId]?.deviceName ?: "Unknown"
            Log.d(TAG, "Peer desconectado: $peerName ($endpointId)")
            _connectedPeers.value = _connectedPeers.value - endpointId
        }
    }
    
    /**
     * Callback para payloads recibidos
     */
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                payload.asBytes()?.let { bytes ->
                    try {
                        val jsonString = bytes.decodeToString()
                        val protocolMessage = json.decodeFromString<MeshProtocolMessage>(jsonString)
                        
                        when (protocolMessage) {
                            is MeshProtocolMessage.DataMessage -> {
                                // Mensaje normal - enviarlo al MeshEngine
                                Log.d(TAG, "Recibido mensaje: ${protocolMessage.message.id}")
                                incomingMessages.trySend(protocolMessage.message)
                            }
                            
                            is MeshProtocolMessage.Inventory -> {
                                // Recibimos inventario de otro peer - verificar qué nos falta
                                Log.d(TAG, "Recibido inventario con ${protocolMessage.messageIds.size} mensajes")
                                val localIds = getLocalInventory?.invoke() ?: emptySet()
                                val missing = protocolMessage.messageIds - localIds
                                if (missing.isNotEmpty()) {
                                    Log.d(TAG, "Solicitando ${missing.size} mensajes faltantes")
                                    sendRequestMissingTo(endpointId, missing)
                                }
                            }
                            
                            is MeshProtocolMessage.RequestMissing -> {
                                // Nos piden mensajes que nos faltan
                                Log.d(TAG, "Peer solicita ${protocolMessage.missingIds.size} mensajes")
                                protocolMessage.missingIds.forEach { messageId ->
                                    getMessageById?.invoke(messageId)?.let { message ->
                                        sendMessageTo(endpointId, message)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando payload: ${e.message}")
                        // Intentar parsear como mensaje legacy (por compatibilidad)
                        try {
                            val message = json.decodeFromString<MeshMessage>(bytes.decodeToString())
                            incomingMessages.trySend(message)
                        } catch (e2: Exception) {
                            Log.e(TAG, "Error parseando mensaje legacy: ${e2.message}")
                        }
                    }
                }
            }
        }
        
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Podemos rastrear progreso aquí si es necesario
        }
    }
    
    /**
     * Envía un mensaje a todos los peers conectados
     */
    fun broadcastMessage(message: MeshMessage) {
        val protocolMessage = MeshProtocolMessage.DataMessage(message)
        val jsonString = json.encodeToString<MeshProtocolMessage>(protocolMessage)
        val payload = Payload.fromBytes(jsonString.encodeToByteArray())
        
        val endpoints = _connectedPeers.value.keys.toList()
        if (endpoints.isNotEmpty()) {
            Log.d(TAG, "Broadcasting mensaje ${message.id} a ${endpoints.size} peers")
            connectionsClient.sendPayload(endpoints, payload)
        }
    }
    
    /**
     * Envía un mensaje a un peer específico
     */
    fun sendMessageTo(endpointId: String, message: MeshMessage) {
        val protocolMessage = MeshProtocolMessage.DataMessage(message)
        val jsonString = json.encodeToString<MeshProtocolMessage>(protocolMessage)
        val payload = Payload.fromBytes(jsonString.encodeToByteArray())
        
        connectionsClient.sendPayload(listOf(endpointId), payload)
    }
    
    /**
     * Envía nuestro inventario a un peer
     */
    private fun sendInventoryTo(endpointId: String) {
        val inventory = getLocalInventory?.invoke() ?: return
        val protocolMessage = MeshProtocolMessage.Inventory(inventory)
        val jsonString = json.encodeToString<MeshProtocolMessage>(protocolMessage)
        val payload = Payload.fromBytes(jsonString.encodeToByteArray())
        
        Log.d(TAG, "Enviando inventario con ${inventory.size} mensajes a $endpointId")
        connectionsClient.sendPayload(listOf(endpointId), payload)
    }
    
    /**
     * Solicita mensajes faltantes a un peer
     */
    private fun sendRequestMissingTo(endpointId: String, missingIds: Set<String>) {
        val protocolMessage = MeshProtocolMessage.RequestMissing(missingIds)
        val jsonString = json.encodeToString<MeshProtocolMessage>(protocolMessage)
        val payload = Payload.fromBytes(jsonString.encodeToByteArray())
        
        connectionsClient.sendPayload(listOf(endpointId), payload)
    }
    
    /**
     * Envía inventario a todos los peers (para sincronización periódica)
     */
    fun broadcastInventory() {
        val inventory = getLocalInventory?.invoke() ?: return
        val protocolMessage = MeshProtocolMessage.Inventory(inventory)
        val jsonString = json.encodeToString<MeshProtocolMessage>(protocolMessage)
        val payload = Payload.fromBytes(jsonString.encodeToByteArray())
        
        val endpoints = _connectedPeers.value.keys.toList()
        if (endpoints.isNotEmpty()) {
            Log.d(TAG, "Broadcasting inventario a ${endpoints.size} peers")
            connectionsClient.sendPayload(endpoints, payload)
        }
    }
    
    /**
     * Desconecta de todos los peers
     */
    private fun disconnectAll() {
        _connectedPeers.value.keys.forEach { endpointId ->
            connectionsClient.disconnectFromEndpoint(endpointId)
        }
        _connectedPeers.value = emptyMap()
        _discoveredPeers.value = emptyMap()
    }
    
    /**
     * Obtiene el número de peers conectados
     */
    fun getConnectedPeerCount(): Int = _connectedPeers.value.size
}
