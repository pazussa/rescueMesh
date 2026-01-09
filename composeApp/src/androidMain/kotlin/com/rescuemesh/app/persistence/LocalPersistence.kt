package com.rescuemesh.app.persistence

import android.content.Context
import android.content.SharedPreferences
import com.rescuemesh.app.model.IncidentRoom
import com.rescuemesh.app.model.MeshMessage
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Persistencia local para mensajes y salas
 * Necesario para store-and-forward cuando la app se reinicia
 */
class LocalPersistence(context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = false
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "rescuemesh_data", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_MESSAGES = "cached_messages"
        private const val KEY_CURRENT_ROOM = "current_room"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_SEEN_MESSAGE_IDS = "seen_message_ids"
        private const val KEY_SAVED_ROOMS = "saved_rooms"
    }
    
    // ============ MENSAJES ============
    
    /**
     * Guarda todos los mensajes en caché
     */
    suspend fun saveMessages(messages: List<MeshMessage>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(messages)
            prefs.edit().putString(KEY_MESSAGES, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga mensajes guardados
     */
    suspend fun loadMessages(): List<MeshMessage> = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(KEY_MESSAGES, null) ?: return@withContext emptyList()
            json.decodeFromString<List<MeshMessage>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Limpia mensajes viejos (más de 24 horas)
     */
    suspend fun cleanOldMessages(maxAgeMillis: Long = 24 * 60 * 60 * 1000) = withContext(Dispatchers.IO) {
        try {
            val messages = loadMessages()
            val cutoff = System.currentTimeMillis() - maxAgeMillis
            val filtered = messages.filter { it.timestamp > cutoff }
            saveMessages(filtered)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // ============ IDs VISTOS (para deduplicación) ============
    
    /**
     * Guarda IDs de mensajes ya vistos
     */
    suspend fun saveSeenMessageIds(ids: Set<String>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(ids.toList())
            prefs.edit().putString(KEY_SEEN_MESSAGE_IDS, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga IDs de mensajes ya vistos
     */
    suspend fun loadSeenMessageIds(): Set<String> = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(KEY_SEEN_MESSAGE_IDS, null) ?: return@withContext emptySet()
            json.decodeFromString<List<String>>(jsonString).toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }
    
    // ============ SALA ACTUAL ============
    
    /**
     * Guarda la sala actual
     */
    suspend fun saveCurrentRoom(room: IncidentRoom?) = withContext(Dispatchers.IO) {
        try {
            if (room == null) {
                prefs.edit().remove(KEY_CURRENT_ROOM).apply()
            } else {
                val jsonString = json.encodeToString(room)
                prefs.edit().putString(KEY_CURRENT_ROOM, jsonString).apply()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga la sala actual
     */
    suspend fun loadCurrentRoom(): IncidentRoom? = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(KEY_CURRENT_ROOM, null) ?: return@withContext null
            json.decodeFromString<IncidentRoom>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // ============ SALAS GUARDADAS ============
    
    /**
     * Guarda historial de salas
     */
    suspend fun saveSavedRooms(rooms: List<IncidentRoom>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(rooms)
            prefs.edit().putString(KEY_SAVED_ROOMS, jsonString).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Carga historial de salas
     */
    suspend fun loadSavedRooms(): List<IncidentRoom> = withContext(Dispatchers.IO) {
        try {
            val jsonString = prefs.getString(KEY_SAVED_ROOMS, null) ?: return@withContext emptyList()
            json.decodeFromString<List<IncidentRoom>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // ============ CONFIGURACIÓN DE USUARIO ============
    
    /**
     * Guarda nombre de usuario
     */
    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }
    
    /**
     * Carga nombre de usuario
     */
    fun loadUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Usuario") ?: "Usuario"
    }
    
    /**
     * Guarda/obtiene device ID persistente
     */
    fun getOrCreateDeviceId(): String {
        var deviceId = prefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            deviceId = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        return deviceId
    }
    
    /**
     * Limpia todos los datos
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
