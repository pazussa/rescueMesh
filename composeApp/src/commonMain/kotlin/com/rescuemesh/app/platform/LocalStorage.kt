package com.rescuemesh.app.platform

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.IncidentRoom

/**
 * Interface for local data persistence
 * Implemented differently on Android (SharedPreferences) and iOS (NSUserDefaults)
 */
interface LocalStorage {
    // Messages
    suspend fun saveMessages(messages: List<MeshMessage>)
    suspend fun loadMessages(): List<MeshMessage>
    suspend fun cleanOldMessages(maxAgeMillis: Long = 24 * 60 * 60 * 1000)
    
    // Seen message IDs (for deduplication)
    suspend fun saveSeenMessageIds(ids: Set<String>)
    suspend fun loadSeenMessageIds(): Set<String>
    
    // Current room
    suspend fun saveCurrentRoom(room: IncidentRoom?)
    suspend fun loadCurrentRoom(): IncidentRoom?
    
    // Saved rooms history
    suspend fun saveSavedRooms(rooms: List<IncidentRoom>)
    suspend fun loadSavedRooms(): List<IncidentRoom>
    
    // User configuration
    fun saveUserName(name: String)
    fun loadUserName(): String
    fun getOrCreateDeviceId(): String
    
    // Clear all
    fun clearAll()
}

/**
 * Factory to create platform-specific LocalStorage
 */
expect class LocalStorageFactory {
    fun create(): LocalStorage
}
