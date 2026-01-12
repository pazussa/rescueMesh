package com.rescuemesh.app.platform

import com.rescuemesh.app.model.MeshMessage
import com.rescuemesh.app.model.IncidentRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import java.util.prefs.Preferences

/**
 * Desktop implementation of LocalStorage
 * Uses Java Preferences for simple values and file storage for complex data
 */
class DesktopLocalStorage : LocalStorage {
    
    private val prefs: Preferences = Preferences.userRoot().node("rescuemesh")
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = true
    }
    
    private val dataDir: File by lazy {
        val home = System.getProperty("user.home")
        File(home, ".rescuemesh").also { it.mkdirs() }
    }
    
    // Messages
    override suspend fun saveMessages(messages: List<MeshMessage>) = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "messages.json")
            file.writeText(json.encodeToString(messages))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override suspend fun loadMessages(): List<MeshMessage> = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "messages.json")
            if (file.exists()) {
                json.decodeFromString(file.readText())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    override suspend fun cleanOldMessages(maxAgeMillis: Long) = withContext(Dispatchers.IO) {
        try {
            val messages = loadMessages()
            val cutoff = System.currentTimeMillis() - maxAgeMillis
            val recent = messages.filter { it.timestamp >= cutoff }
            saveMessages(recent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Seen message IDs
    override suspend fun saveSeenMessageIds(ids: Set<String>) = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "seen_ids.json")
            file.writeText(json.encodeToString(ids.toList()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override suspend fun loadSeenMessageIds(): Set<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "seen_ids.json")
            if (file.exists()) {
                json.decodeFromString<List<String>>(file.readText()).toSet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }
    
    // Current room
    override suspend fun saveCurrentRoom(room: IncidentRoom?): Unit = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "current_room.json")
            if (room != null) {
                file.writeText(json.encodeToString(room))
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override suspend fun loadCurrentRoom(): IncidentRoom? = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "current_room.json")
            if (file.exists()) {
                json.decodeFromString(file.readText())
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Saved rooms
    override suspend fun saveSavedRooms(rooms: List<IncidentRoom>) = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "saved_rooms.json")
            file.writeText(json.encodeToString(rooms))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override suspend fun loadSavedRooms(): List<IncidentRoom> = withContext(Dispatchers.IO) {
        try {
            val file = File(dataDir, "saved_rooms.json")
            if (file.exists()) {
                json.decodeFromString(file.readText())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    // User config
    override fun saveUserName(name: String) {
        prefs.put("user_name", name)
        prefs.flush()
    }
    
    override fun loadUserName(): String {
        return prefs.get("user_name", "")
    }
    
    override fun getOrCreateDeviceId(): String {
        var id = prefs.get("device_id", "")
        if (id.isEmpty()) {
            id = "desktop-${UUID.randomUUID()}"
            prefs.put("device_id", id)
            prefs.flush()
        }
        return id
    }
    
    override fun clearAll() {
        try {
            prefs.clear()
            prefs.flush()
            dataDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

/**
 * Factory implementation for Desktop
 */
actual class LocalStorageFactory {
    actual fun create(): LocalStorage = DesktopLocalStorage()
}
