package com.rescuemesh.app.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Mensaje de la red mesh
 */
@Serializable
data class MeshMessage(
    val id: String,                          // UUID único del mensaje
    val roomId: String,                      // ID del Incident Room
    val senderId: String,                    // ID del dispositivo emisor
    val senderName: String,                  // Nombre del usuario
    val type: MessageType,                   // Tipo de mensaje
    val content: MessageContent,             // Contenido específico
    val timestamp: Long,                     // Timestamp de creación
    val ttl: Int = 5,                        // Time to live (saltos restantes)
    val hopCount: Int = 0,                   // Número de saltos realizados
    val priority: MessagePriority,           // Prioridad del mensaje
    val latitude: Double? = null,            // Ubicación opcional
    val longitude: Double? = null
) {
    fun decrementTtl(): MeshMessage = copy(ttl = ttl - 1, hopCount = hopCount + 1)
    
    fun isExpired(): Boolean = ttl <= 0
}

/**
 * Contenido del mensaje según su tipo
 */
@Serializable
sealed class MessageContent {
    
    @Serializable
    data class Sos(
        val category: SosCategory,
        val description: String,
        val peopleCount: Int = 1
    ) : MessageContent()
    
    @Serializable
    data class ImOk(
        val message: String = "Estoy bien"
    ) : MessageContent()
    
    @Serializable
    data class ResourceRequest(
        val resourceType: ResourceType,
        val quantity: Int = 1,
        val urgent: Boolean = false,
        val description: String = ""
    ) : MessageContent()
    
    @Serializable
    data class DangerReport(
        val dangerType: DangerType,
        val severity: Int = 5,  // 1-10
        val description: String,
        val isBlocking: Boolean = false
    ) : MessageContent()
    
    @Serializable
    data class Chat(
        val text: String
    ) : MessageContent()
}

/**
 * Información de un Incident Room
 */
@Serializable
data class IncidentRoom(
    val id: String,                    // ID único de la sala
    val name: String,                  // Nombre descriptivo
    val pin: String,                   // PIN de 6 dígitos para unirse
    val createdAt: Long,               // Timestamp de creación
    val creatorId: String,             // ID del creador
    val description: String = ""       // Descripción del incidente
)

/**
 * Información de un peer en la red
 */
@Serializable
data class MeshPeer(
    val endpointId: String,            // ID de Nearby Connections
    val deviceId: String,              // ID del dispositivo
    val deviceName: String,            // Nombre del dispositivo/usuario
    val roomId: String,                // Sala en la que está
    val lastSeen: Long                 // Última vez visto
)

/**
 * Estado de conexión del peer
 */
@Serializable
enum class PeerConnectionState {
    DISCOVERED,
    CONNECTING,
    CONNECTED,
    DISCONNECTED
}
