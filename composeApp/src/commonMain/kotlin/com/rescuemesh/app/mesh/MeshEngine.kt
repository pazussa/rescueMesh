package com.rescuemesh.app.mesh

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.MessagePriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Motor de mesh compartido (commonMain)
 * Maneja: deduplicación, TTL, colas de prioridad, store-and-forward
 */
class MeshEngine {
    
    // Mensajes recibidos (deduplicados)
    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()
    
    // Set de IDs ya vistos para deduplicación rápida
    private val seenMessageIds = mutableSetOf<String>()
    
    // Cola de mensajes pendientes de reenviar
    private val forwardQueue = mutableListOf<MeshMessage>()
    
    // Mutex para operaciones thread-safe
    private val mutex = Mutex()
    
    // Callback para enviar mensajes a la capa de transporte
    var onMessageToForward: ((MeshMessage) -> Unit)? = null
    
    /**
     * Procesa un mensaje recibido de la red
     * @return true si el mensaje es nuevo y debe mostrarse
     */
    suspend fun processIncomingMessage(message: MeshMessage): Boolean {
        return mutex.withLock {
            // Deduplicación: si ya lo vimos, ignorar
            if (seenMessageIds.contains(message.id)) {
                return@withLock false
            }
            
            // Marcar como visto
            seenMessageIds.add(message.id)
            
            // Agregar a la lista ordenada por prioridad
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(message)
            currentMessages.sortWith(compareBy({ it.priority.value }, { -it.timestamp }))
            _messages.value = currentMessages
            
            // Si aún tiene TTL, preparar para reenvío
            if (!message.isExpired()) {
                val messageToForward = message.decrementTtl()
                if (!messageToForward.isExpired()) {
                    forwardQueue.add(messageToForward)
                    // Ordenar cola por prioridad (SOS primero)
                    forwardQueue.sortBy { it.priority.value }
                }
            }
            
            true
        }
    }
    
    /**
     * Crea y envía un mensaje propio
     */
    suspend fun sendMessage(message: MeshMessage) {
        mutex.withLock {
            // Agregar a nuestra lista
            seenMessageIds.add(message.id)
            
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(message)
            currentMessages.sortWith(compareBy({ it.priority.value }, { -it.timestamp }))
            _messages.value = currentMessages
            
            // Enviar a la red
            onMessageToForward?.invoke(message)
        }
    }
    
    /**
     * Obtiene el siguiente mensaje a reenviar (priorizado)
     */
    suspend fun getNextMessageToForward(): MeshMessage? {
        return mutex.withLock {
            forwardQueue.removeFirstOrNull()
        }
    }
    
    /**
     * Procesa la cola de reenvío
     */
    suspend fun processForwardQueue() {
        while (true) {
            val message = getNextMessageToForward() ?: break
            onMessageToForward?.invoke(message)
        }
    }
    
    /**
     * Obtiene mensajes filtrados por sala
     */
    fun getMessagesForRoom(roomId: String): List<MeshMessage> {
        return _messages.value.filter { it.roomId == roomId }
    }
    
    /**
     * Limpia mensajes antiguos (más de 24 horas)
     */
    suspend fun cleanOldMessages(maxAgeMillis: Long = 24 * 60 * 60 * 1000) {
        mutex.withLock {
            val cutoffTime = System.currentTimeMillis() - maxAgeMillis
            
            val currentMessages = _messages.value.filter { it.timestamp > cutoffTime }
            _messages.value = currentMessages
            
            // También limpiar IDs antiguos del set de deduplicación
            // (esto es simplificado, en producción necesitarías timestamps por ID)
        }
    }
    
    /**
     * Solicita mensajes faltantes a peers (inventory/request-missing)
     */
    fun getMessageInventory(): Set<String> {
        return seenMessageIds.toSet()
    }
    
    /**
     * Restaura IDs vistos desde persistencia
     */
    fun restoreSeenIds(ids: Set<String>) {
        seenMessageIds.addAll(ids)
    }
    
    /**
     * Verifica qué mensajes nos faltan comparando con un inventario remoto
     */
    fun getMissingMessageIds(remoteInventory: Set<String>): Set<String> {
        return remoteInventory - seenMessageIds
    }
    
    /**
     * Busca un mensaje por ID para responder a request-missing
     */
    fun getMessageById(id: String): MeshMessage? {
        return _messages.value.find { it.id == id }
    }
    
    /**
     * Limpia todo el estado (al salir de una sala)
     */
    suspend fun clear() {
        mutex.withLock {
            _messages.value = emptyList()
            seenMessageIds.clear()
            forwardQueue.clear()
        }
    }
    
    /**
     * Obtiene estadísticas del mesh
     */
    fun getStats(): MeshStats {
        return MeshStats(
            totalMessages = _messages.value.size,
            pendingForward = forwardQueue.size,
            sosCount = _messages.value.count { it.priority == MessagePriority.CRITICAL || it.priority == MessagePriority.HIGH },
            dangerReports = _messages.value.count { it.priority == MessagePriority.MEDIUM }
        )
    }
    
    /**
     * Obtiene número de mensajes pendientes de reenvío
     */
    fun getPendingForwardCount(): Int = forwardQueue.size
}

data class MeshStats(
    val totalMessages: Int,
    val pendingForward: Int,
    val sosCount: Int,
    val dangerReports: Int
)
