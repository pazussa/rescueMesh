package com.rescuemesh.app.platform

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.IncidentRoom
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*

/**
 * iOS implementation of LocalStorage using NSUserDefaults
 */
class IOSLocalStorage : LocalStorage {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = false
    }
    
    private val defaults = NSUserDefaults.standardUserDefaults
    
    companion object {
        private const val KEY_MESSAGES = "rescuemesh_messages"
        private const val KEY_CURRENT_ROOM = "rescuemesh_current_room"
        private const val KEY_USER_NAME = "rescuemesh_user_name"
        private const val KEY_DEVICE_ID = "rescuemesh_device_id"
        private const val KEY_SEEN_MESSAGE_IDS = "rescuemesh_seen_ids"
        private const val KEY_SAVED_ROOMS = "rescuemesh_saved_rooms"
    }
    
    override suspend fun saveMessages(messages: List<MeshMessage>) {
        try {
            val jsonString = json.encodeToString(messages)
            defaults.setObject(jsonString, KEY_MESSAGES)
            defaults.synchronize()
        } catch (e: Exception) {
            println("Error saving messages: ${e.message}")
        }
    }
    
    override suspend fun loadMessages(): List<MeshMessage> {
        return try {
            val jsonString = defaults.stringForKey(KEY_MESSAGES) ?: return emptyList()
            json.decodeFromString<List<MeshMessage>>(jsonString)
        } catch (e: Exception) {
            println("Error loading messages: ${e.message}")
            emptyList()
        }
    }
    
    override suspend fun cleanOldMessages(maxAgeMillis: Long) {
        try {
            val messages = loadMessages()
            val cutoff = currentTimeMillis() - maxAgeMillis
            val filtered = messages.filter { it.timestamp > cutoff }
            saveMessages(filtered)
        } catch (e: Exception) {
            println("Error cleaning old messages: ${e.message}")
        }
    }
    
    override suspend fun saveSeenMessageIds(ids: Set<String>) {
        try {
            val jsonString = json.encodeToString(ids.toList())
            defaults.setObject(jsonString, KEY_SEEN_MESSAGE_IDS)
            defaults.synchronize()
        } catch (e: Exception) {
            println("Error saving seen IDs: ${e.message}")
        }
    }
    
    override suspend fun loadSeenMessageIds(): Set<String> {
        return try {
            val jsonString = defaults.stringForKey(KEY_SEEN_MESSAGE_IDS) ?: return emptySet()
            json.decodeFromString<List<String>>(jsonString).toSet()
        } catch (e: Exception) {
            println("Error loading seen IDs: ${e.message}")
            emptySet()
        }
    }
    
    override suspend fun saveCurrentRoom(room: IncidentRoom?) {
        try {
            if (room == null) {
                defaults.removeObjectForKey(KEY_CURRENT_ROOM)
            } else {
                val jsonString = json.encodeToString(room)
                defaults.setObject(jsonString, KEY_CURRENT_ROOM)
            }
            defaults.synchronize()
        } catch (e: Exception) {
            println("Error saving current room: ${e.message}")
        }
    }
    
    override suspend fun loadCurrentRoom(): IncidentRoom? {
        return try {
            val jsonString = defaults.stringForKey(KEY_CURRENT_ROOM) ?: return null
            json.decodeFromString<IncidentRoom>(jsonString)
        } catch (e: Exception) {
            println("Error loading current room: ${e.message}")
            null
        }
    }
    
    override suspend fun saveSavedRooms(rooms: List<IncidentRoom>) {
        try {
            val jsonString = json.encodeToString(rooms)
            defaults.setObject(jsonString, KEY_SAVED_ROOMS)
            defaults.synchronize()
        } catch (e: Exception) {
            println("Error saving saved rooms: ${e.message}")
        }
    }
    
    override suspend fun loadSavedRooms(): List<IncidentRoom> {
        return try {
            val jsonString = defaults.stringForKey(KEY_SAVED_ROOMS) ?: return emptyList()
            json.decodeFromString<List<IncidentRoom>>(jsonString)
        } catch (e: Exception) {
            println("Error loading saved rooms: ${e.message}")
            emptyList()
        }
    }
    
    override fun saveUserName(name: String) {
        defaults.setObject(name, KEY_USER_NAME)
        defaults.synchronize()
    }
    
    override fun loadUserName(): String {
        return defaults.stringForKey(KEY_USER_NAME) ?: "User"
    }
    
    override fun getOrCreateDeviceId(): String {
        var deviceId = defaults.stringForKey(KEY_DEVICE_ID)
        if (deviceId == null) {
            deviceId = randomUUID()
            defaults.setObject(deviceId, KEY_DEVICE_ID)
            defaults.synchronize()
        }
        return deviceId
    }
    
    override fun clearAll() {
        defaults.removeObjectForKey(KEY_MESSAGES)
        defaults.removeObjectForKey(KEY_CURRENT_ROOM)
        defaults.removeObjectForKey(KEY_USER_NAME)
        defaults.removeObjectForKey(KEY_DEVICE_ID)
        defaults.removeObjectForKey(KEY_SEEN_MESSAGE_IDS)
        defaults.removeObjectForKey(KEY_SAVED_ROOMS)
        defaults.synchronize()
    }
}

/**
 * Factory for iOS LocalStorage
 */
actual class LocalStorageFactory {
    actual fun create(): LocalStorage {
        return IOSLocalStorage()
    }
}
