package com.rescuemesh.app.mesh

import com.rescuemesh.app.model.IncidentRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Gestor de Incident Rooms
 */
class RoomManager {
    
    private val _currentRoom = MutableStateFlow<IncidentRoom?>(null)
    val currentRoom: StateFlow<IncidentRoom?> = _currentRoom.asStateFlow()
    
    private val _savedRooms = MutableStateFlow<List<IncidentRoom>>(emptyList())
    val savedRooms: StateFlow<List<IncidentRoom>> = _savedRooms.asStateFlow()
    
    /**
     * Genera un ID único para el room
     */
    private fun generateRoomId(): String {
        return buildString {
            repeat(8) {
                append(('A'..'Z').random())
            }
        }
    }
    
    /**
     * Genera un PIN de 6 dígitos
     */
    private fun generatePin(): String {
        return buildString {
            repeat(6) {
                append(Random.nextInt(0, 10))
            }
        }
    }
    
    /**
     * Crea un nuevo Incident Room
     */
    fun createRoom(name: String, description: String, creatorId: String): IncidentRoom {
        val room = IncidentRoom(
            id = generateRoomId(),
            name = name,
            pin = generatePin(),
            createdAt = System.currentTimeMillis(),
            creatorId = creatorId,
            description = description
        )
        
        _currentRoom.value = room
        saveRoom(room)
        
        return room
    }
    
    /**
     * Une al usuario a una sala existente
     */
    fun joinRoom(room: IncidentRoom): Boolean {
        _currentRoom.value = room
        saveRoom(room)
        return true
    }
    
    /**
     * Valida un PIN para unirse a una sala
     */
    fun validatePin(roomId: String, pin: String, room: IncidentRoom): Boolean {
        return room.id == roomId && room.pin == pin
    }
    
    /**
     * Sale de la sala actual
     */
    fun leaveRoom() {
        _currentRoom.value = null
    }
    
    /**
     * Guarda una sala en historial
     */
    private fun saveRoom(room: IncidentRoom) {
        val current = _savedRooms.value.toMutableList()
        // Evitar duplicados
        current.removeAll { it.id == room.id }
        current.add(0, room)
        // Mantener solo las últimas 10
        if (current.size > 10) {
            current.removeLast()
        }
        _savedRooms.value = current
    }
    
    /**
     * Genera datos QR para compartir sala
     */
    fun generateQrData(room: IncidentRoom): String {
        return "rescuemesh://${room.id}/${room.pin}/${room.name}"
    }
    
    /**
     * Parsea datos QR
     */
    fun parseQrData(data: String): Triple<String, String, String>? {
        return try {
            val cleaned = data.removePrefix("rescuemesh://")
            val parts = cleaned.split("/")
            if (parts.size >= 3) {
                Triple(parts[0], parts[1], parts[2])
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
