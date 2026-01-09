package com.rescuemesh.app.platform

import android.content.Context
import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.IncidentRoom
import com.rescuemesh.app.persistence.LocalPersistence

/**
 * Android implementation of LocalStorage using SharedPreferences
 */
class AndroidLocalStorage(
    private val persistence: LocalPersistence
) : LocalStorage {
    
    override suspend fun saveMessages(messages: List<MeshMessage>) {
        persistence.saveMessages(messages)
    }
    
    override suspend fun loadMessages(): List<MeshMessage> {
        return persistence.loadMessages()
    }
    
    override suspend fun cleanOldMessages(maxAgeMillis: Long) {
        persistence.cleanOldMessages(maxAgeMillis)
    }
    
    override suspend fun saveSeenMessageIds(ids: Set<String>) {
        persistence.saveSeenMessageIds(ids)
    }
    
    override suspend fun loadSeenMessageIds(): Set<String> {
        return persistence.loadSeenMessageIds()
    }
    
    override suspend fun saveCurrentRoom(room: IncidentRoom?) {
        persistence.saveCurrentRoom(room)
    }
    
    override suspend fun loadCurrentRoom(): IncidentRoom? {
        return persistence.loadCurrentRoom()
    }
    
    override suspend fun saveSavedRooms(rooms: List<IncidentRoom>) {
        persistence.saveSavedRooms(rooms)
    }
    
    override suspend fun loadSavedRooms(): List<IncidentRoom> {
        return persistence.loadSavedRooms()
    }
    
    override fun saveUserName(name: String) {
        persistence.saveUserName(name)
    }
    
    override fun loadUserName(): String {
        return persistence.loadUserName()
    }
    
    override fun getOrCreateDeviceId(): String {
        return persistence.getOrCreateDeviceId()
    }
    
    override fun clearAll() {
        persistence.clearAll()
    }
}

/**
 * Factory for Android LocalStorage
 */
actual class LocalStorageFactory {
    actual fun create(): LocalStorage {
        val context = AndroidPlatformContext.applicationContext
            ?: throw IllegalStateException("AndroidPlatformContext not initialized")
        return AndroidLocalStorage(LocalPersistence(context))
    }
}
